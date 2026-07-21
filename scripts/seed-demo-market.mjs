import { spawnSync } from 'node:child_process'

const apiBase = process.env.AIART_API_BASE || 'http://127.0.0.1:8080/api'
const mysqlBin = process.env.MYSQL_BIN || 'D:/Type_Software_tool/Mysql_8.0.44/MySQL Server 8.0/bin/mysql.exe'
const mysqlPassword = process.env.AIART_MYSQL_PASSWORD
const demoPassword = process.env.AIART_DEMO_PASSWORD

if (!demoPassword) {
  throw new Error('AIART_DEMO_PASSWORD is required')
}

if (!mysqlPassword) {
  throw new Error('AIART_MYSQL_PASSWORD is required')
}

function mysql(sql) {
  const result = spawnSync(mysqlBin, [
    '-uroot',
    '--default-character-set=utf8mb4',
    '-N',
    '-B',
    'aiart_codex_platform'
  ], {
    input: sql,
    encoding: 'utf8',
    env: { ...process.env, MYSQL_PWD: mysqlPassword },
    maxBuffer: 10 * 1024 * 1024
  })
  if (result.status !== 0) {
    throw new Error(result.stderr || `mysql exited with ${result.status}`)
  }
  return result.stdout.trim()
}

function scalar(sql) {
  return mysql(sql).split(/\r?\n/).filter(Boolean)[0] || ''
}

function rows(sql) {
  const output = mysql(sql)
  return output ? output.split(/\r?\n/).filter(Boolean).map((line) => line.split('\t')) : []
}

function quote(value) {
  return `'${String(value ?? '').replaceAll('\\', '\\\\').replaceAll("'", "''")}'`
}

async function rawApi(method, path, token, payload) {
  const response = await fetch(`${apiBase}${path}`, {
    method,
    headers: {
      ...(payload === undefined ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: payload === undefined ? undefined : JSON.stringify(payload)
  })
  const body = await response.json()
  return body
}

async function api(method, path, token, payload) {
  const body = await rawApi(method, path, token, payload)
  if (body.code !== 0) {
    throw new Error(`${method} ${path}: ${body.message}`)
  }
  return body.data
}

const demoUsers = [
  { username: 'demo_watercolor', displayName: '演示作者·青禾' },
  { username: 'demo_cinema', displayName: '演示导演·北屿' },
  { username: 'demo_ink', displayName: '演示画师·墨川' },
  { username: 'demo_pixel', displayName: '演示设计师·星野' },
  { username: 'demo_brand', displayName: '演示工作室·折光' }
]

async function ensureUser(definition) {
  let login = await rawApi('POST', '/auth/login', '', { username: definition.username, password: demoPassword })
  if (login.code !== 0) {
    const registered = await rawApi('POST', '/auth/register', '', {
      username: definition.username,
      password: demoPassword,
      displayName: definition.displayName
    })
    if (registered.code !== 0 && !String(registered.message).toLowerCase().includes('exist')) {
      throw new Error(`Cannot create ${definition.username}: ${registered.message}`)
    }
    login = await rawApi('POST', '/auth/login', '', { username: definition.username, password: demoPassword })
  }
  if (login.code !== 0) throw new Error(`Cannot login ${definition.username}: ${login.message}`)
  const id = scalar(`SELECT id FROM users WHERE username=${quote(definition.username)} LIMIT 1;`)
  return { ...definition, id, token: login.data.token, artworks: [] }
}

const users = []
for (const definition of demoUsers) {
  users.push(await ensureUser(definition))
}

const imageUrls = rows(`
  SELECT image_url
  FROM tag_preview
  WHERE image_url IS NOT NULL AND image_url <> ''
  ORDER BY is_cover DESC, sort_order ASC, id ASC
  LIMIT 30;
`).map(([url]) => url)

if (imageUrls.length < 10) {
  throw new Error('At least 10 tag preview images are required before seeding demo content')
}

const tagRows = rows(`SELECT id, name FROM tag WHERE status='ACTIVE';`)
const tagIds = new Map(tagRows.map(([id, name]) => [name, id]))
const requiredTags = [
  'watercolor', 'cinematic', 'ink wash', 'golden hour', 'pixel art', 'minimalist',
  'cyberpunk', 'anime', 'product', 'studio light', 'retro poster', 'dreamy',
  'high detail', 'wide shot', 'character', 'landscape'
]
for (const name of requiredTags) {
  if (!tagIds.has(name)) throw new Error(`Missing required tag: ${name}`)
}

const artworkDefinitions = [
  ['雨后山城水彩速写', 'watercolor city after rain, travel sketch, soft pigment'],
  ['晨雾湖畔旅行手帐', 'watercolor lake, morning mist, handwritten travel journal'],
  ['电影感夜行人物', 'cinematic portrait, night street, dramatic rim light'],
  ['黄金时刻公路故事', 'cinematic road trip, golden hour, wide shot'],
  ['山水留白练习', 'ink wash landscape, negative space, Song dynasty mood'],
  ['水墨花鸟小品', 'ink wash bird and flower, elegant brushwork'],
  ['像素森林关卡', 'pixel art forest level, game environment, colorful tiles'],
  ['像素冒险角色组', 'pixel art adventurer characters, sprite concept'],
  ['极简香氛产品图', 'minimal product photography, studio light, clean composition'],
  ['霓虹饮料品牌视觉', 'cyberpunk beverage branding, neon light, product poster']
]

const artworkSql = []
const artworkTagSql = []
for (let index = 0; index < artworkDefinitions.length; index++) {
  const user = users[Math.floor(index / 2)]
  const artworkId = String(880000000000000001n + BigInt(index))
  const relationId = String(881000000000000001n + BigInt(index))
  const [title, prompt] = artworkDefinitions[index]
  const imageUrl = imageUrls[index]
  const primaryTag = requiredTags[index % requiredTags.length]
  user.artworks.push({ id: artworkId, title, imageUrl })
  artworkSql.push(`
    INSERT INTO artwork
      (id, user_id, title, prompt_text, negative_prompt, image_url, thumbnail_url,
       generation_params_json, source_job_id, visibility, status, created_at)
    VALUES
      (${artworkId}, ${user.id}, ${quote(`【演示】${title}`)}, ${quote(prompt)},
       'low quality, watermark, text artifacts', ${quote(imageUrl)}, ${quote(imageUrl)},
       JSON_OBJECT('seed', 'demo-market', 'source', 'tag-preview'), NULL, 'PUBLIC', 'ACTIVE', NOW())
    ON DUPLICATE KEY UPDATE
      user_id=VALUES(user_id), title=VALUES(title), prompt_text=VALUES(prompt_text),
      image_url=VALUES(image_url), thumbnail_url=VALUES(thumbnail_url),
      generation_params_json=VALUES(generation_params_json), visibility='PUBLIC', status='ACTIVE';
  `)
  artworkTagSql.push(`
    INSERT INTO artwork_tag (id, artwork_id, tag_id, user_id, created_at)
    VALUES (${relationId}, ${artworkId}, ${tagIds.get(primaryTag)}, ${user.id}, NOW())
    ON DUPLICATE KEY UPDATE tag_id=VALUES(tag_id), user_id=VALUES(user_id);
  `)
}

mysql(`
  ${artworkSql.join('\n')}
  ${artworkTagSql.join('\n')}
  UPDATE users SET point_balance=1000.00 WHERE id IN (${users.map((user) => user.id).join(',')});
  UPDATE point_account SET balance=1000.00, frozen_balance=0.00 WHERE user_id IN (${users.map((user) => user.id).join(',')});
`)

const styleDefinitions = [
  {
    owner: 0, name: '【演示】水彩旅行日记', price: 0,
    description: '面向城市漫游、自然风景和旅行手帐的清透水彩风格合集。',
    statement: '保留水彩自然晕染、纸张颗粒与轻盈留白，让画面具有旅行记录感。',
    prompt: 'watercolor illustration, translucent pigment, travel sketch, textured paper, soft edges',
    negative: 'photorealistic, plastic texture, oversaturated, hard digital edges',
    tags: ['watercolor', 'landscape', 'dreamy']
  },
  {
    owner: 1, name: '【演示】电影感人物叙事', price: 60,
    description: '适合剧情人物、短片概念图和氛围海报的电影化视觉包。',
    statement: '强调光影层次、叙事构图与角色环境关系。',
    prompt: 'cinematic portrait, dramatic lighting, professional color grading, shallow depth of field',
    negative: 'flat lighting, casual snapshot, low detail, distorted face',
    tags: ['cinematic', 'character', 'high detail']
  },
  {
    owner: 2, name: '【演示】新中式水墨意境', price: 0,
    description: '山水、花鸟和东方幻想主题的现代水墨成果集合。',
    statement: '以墨色层次和留白塑造安静、含蓄的东方空间。',
    prompt: 'Chinese ink wash painting, expressive brushwork, negative space, poetic atmosphere',
    negative: '3d render, western oil painting, neon colors, cluttered composition',
    tags: ['ink wash', 'landscape', 'dreamy']
  },
  {
    owner: 1, name: '【演示】复古胶片街拍', price: 30,
    description: '复古城市街拍、生活纪实和青春故事的胶片色彩方案。',
    statement: '低反差、自然颗粒与略带偏色的生活化镜头。',
    prompt: 'retro street photography, analog film grain, warm palette, candid moment',
    negative: 'sterile studio, hyper sharp digital look, heavy HDR',
    tags: ['retro poster', 'golden hour', 'warm palette']
  },
  {
    owner: 3, name: '【演示】像素冒险世界', price: 0,
    description: '适合横版冒险、RPG 地图和像素角色设计的成果集合。',
    statement: '统一有限色板、清晰轮廓和可读的游戏层次。',
    prompt: 'pixel art, retro game environment, readable silhouette, limited color palette',
    negative: 'photorealistic, blurry pixels, vector smoothness, noisy composition',
    tags: ['pixel art', 'fantasy art', 'wide shot']
  },
  {
    owner: 4, name: '【演示】极简产品棚拍', price: 50,
    description: '香氛、数码产品和生活方式品牌的干净产品视觉模板。',
    statement: '用简洁几何、材质细节与克制光线突出产品本身。',
    prompt: 'minimal product photography, studio light, clean lines, premium material detail',
    negative: 'busy background, random props, harsh reflections, unreadable label',
    tags: ['minimalist', 'product', 'studio light']
  },
  {
    owner: 4, name: '【演示】赛博霓虹品牌夜景', price: 80,
    description: '夜间城市、潮流海报和科技品牌视觉的霓虹风格包。',
    statement: '使用冷暖霓虹对比、湿润反射和未来城市符号。',
    prompt: 'cyberpunk city, neon light, rainy reflections, futuristic commercial poster',
    negative: 'daylight, rural scene, muted palette, vintage paper',
    tags: ['cyberpunk', 'neon light', 'rainy weather']
  },
  {
    owner: 0, name: '【演示】梦境绘本角色', price: 0,
    description: '儿童绘本、轻幻想角色和温柔故事场景的成果集合。',
    statement: '柔和色彩、友善角色造型与清晰的故事焦点。',
    prompt: 'dreamy storybook character, soft light, pastel colors, gentle fantasy illustration',
    negative: 'horror, harsh shadows, photorealistic skin, dark gore',
    tags: ['anime', 'dreamy', 'pastel colors']
  }
]

async function ensureStylePackage(definition, index) {
  const owner = users[definition.owner]
  const featured = owner.artworks[index % owner.artworks.length]
  const collaborator = users[(definition.owner + 1) % users.length]
  const payload = {
    name: definition.name,
    description: definition.description,
    coverImageUrl: featured.imageUrl,
    styleStatement: definition.statement,
    promptGuide: definition.prompt,
    negativePromptGuide: definition.negative,
    featuredArtworkId: featured.id,
    pricePoints: definition.price,
    tagIds: definition.tags.map((name) => tagIds.get(name)),
    tagNames: [],
    collaborators: [{ userId: collaborator.id, role: 'CONTRIBUTOR' }]
  }
  let packageId = scalar(`SELECT id FROM style_package WHERE user_id=${owner.id} AND name=${quote(definition.name)} LIMIT 1;`)
  const created = !packageId
  if (created) {
    await api('POST', '/style-packages', owner.token, payload)
    packageId = scalar(`SELECT id FROM style_package WHERE user_id=${owner.id} AND name=${quote(definition.name)} LIMIT 1;`)
    if (index < 3) {
      await api('PUT', `/style-packages/${packageId}`, owner.token, {
        ...payload,
        description: `${definition.description} 已补充人物、风景与物品三类示例。`
      })
    }
  }
  const status = scalar(`SELECT status FROM style_package WHERE id=${packageId};`)
  if (status !== 'PUBLISHED') await api('POST', `/style-packages/${packageId}/publish`, owner.token)
  return { ...definition, id: packageId, owner, payload }
}

const packages = []
for (let index = 0; index < styleDefinitions.length; index++) {
  packages.push(await ensureStylePackage(styleDefinitions[index], index))
}

async function ensureStyleAccess(stylePackage, viewer) {
  if (stylePackage.price <= 0 || stylePackage.owner.id === viewer.id) return
  const exists = scalar(`SELECT id FROM style_package_access WHERE style_package_id=${stylePackage.id} AND user_id=${viewer.id} LIMIT 1;`)
  if (!exists) await api('POST', `/style-packages/${stylePackage.id}/exchange`, viewer.token)
}

async function ensureStyleSubmission(stylePackage, submitter, artwork, decision = 'APPROVED') {
  await ensureStyleAccess(stylePackage, submitter)
  let submissionId = scalar(`SELECT id FROM style_package_submission WHERE style_package_id=${stylePackage.id} AND artwork_id=${artwork.id} LIMIT 1;`)
  if (!submissionId) {
    await api('POST', `/style-packages/${stylePackage.id}/submissions`, submitter.token, {
      artworkId: artwork.id,
      note: '【演示】用于验证风格包成果投稿、审核和作品集收录流程。'
    })
    submissionId = scalar(`SELECT id FROM style_package_submission WHERE style_package_id=${stylePackage.id} AND artwork_id=${artwork.id} LIMIT 1;`)
  }
  const status = scalar(`SELECT status FROM style_package_submission WHERE id=${submissionId};`)
  if (status === 'PENDING') {
    await api('POST', `/style-packages/submissions/${submissionId}/review`, stylePackage.owner.token, {
      status: decision,
      comment: decision === 'APPROVED' ? '【演示】风格一致，收入成果集。' : '【演示】风格偏差较大，暂不收录。'
    })
  }
}

for (let index = 0; index < packages.length; index++) {
  const stylePackage = packages[index]
  await ensureStyleSubmission(stylePackage, stylePackage.owner, stylePackage.owner.artworks[1])
  if (index < 5) {
    const contributor = users[(styleDefinitions[index].owner + 2) % users.length]
    await ensureStyleSubmission(stylePackage, contributor, contributor.artworks[0], index === 4 ? 'REJECTED' : 'APPROVED')
    await ensureStyleAccess(stylePackage, contributor)
    await api('POST', `/style-packages/${stylePackage.id}/reviews`, contributor.token, {
      rating: 4 + (index % 2),
      comment: `【演示】${index % 2 ? '风格约束清晰，适合直接复用。' : '预览覆盖充分，中文说明也很好理解。'}`
    })
  }
}

const taskDefinitions = [
  ['【演示】茶饮品牌夏季主视觉', '为新品茶饮制作清爽、有年轻感的夏季宣传主视觉。', '画面需要包含杯装饮品、冰块、水果元素；横版 16:9；不得出现第三方商标。', 360, 0],
  ['【演示】独立游戏森林关卡概念图', '设计一个适合横版冒险游戏的森林关卡视觉。', '需要前景、中景、远景层次；提供白天版；强调可玩区域可读性。', 680, 3],
  ['【演示】城市文旅水彩海报', '以城市地标和生活气息为主题制作水彩旅行海报。', '竖版海报；保留上方标题空间；色彩轻盈；至少包含一个人物。', 420, 2],
  ['【演示】科技产品发布会背景', '制作适合大屏展示的科技感产品发布会背景图。', '深色背景；中心保留产品区域；避免细碎文字；超宽构图。', 900, 4],
  ['【演示】电影短片角色气氛稿', '为悬疑短片的主要角色绘制夜间气氛概念。', '半身人物；雨夜街道；冷暖光对比；不要直接模仿具体演员。', 520, 1],
  ['【演示】新中式香氛包装概念', '探索水墨与现代极简结合的香氛包装方向。', '产品正面可见；留出品牌文字区；黑白灰与一点朱红。', 480, 2],
  ['【演示】儿童绘本封面角色', '绘制温柔、友好的森林故事绘本封面。', '主角是一名旅行儿童与一只小动物；暖色；标题区清晰。', 300, 0],
  ['【演示】复古街区活动宣传图', '为周末街区市集制作复古胶片感宣传视觉。', '城市生活场景；自然人物互动；不要出现可识别品牌。', 260, 1],
  ['【演示】电商产品纯净场景图', '为家居小产品制作干净的电商详情页场景图。', '浅色背景；柔和阴影；突出材质；方形构图。', 220, 4],
  ['【演示】赛博城市交通工具设定', '设计未来城市中的公共交通工具概念。', '三分之二侧视角；夜景；体现载客结构；附带环境尺度参照。', 760, 3]
]

async function ensureTask(definition, index) {
  const [title, description, requirementsText, budgetPoints, ownerIndex] = definition
  const owner = users[ownerIndex]
  const payload = {
    title,
    description,
    requirementsText,
    budgetPoints,
    deadline: new Date(Date.now() + (10 + index * 3) * 86400000).toISOString().slice(0, 19)
  }
  let taskId = scalar(`SELECT id FROM enterprise_task WHERE publisher_id=${owner.id} AND title=${quote(title)} LIMIT 1;`)
  if (!taskId) {
    await api('POST', '/tasks', owner.token, payload)
    taskId = scalar(`SELECT id FROM enterprise_task WHERE publisher_id=${owner.id} AND title=${quote(title)} LIMIT 1;`)
  }
  const status = scalar(`SELECT status FROM enterprise_task WHERE id=${taskId};`)
  if (status === 'DRAFT') await api('POST', `/tasks/${taskId}/publish`, owner.token)
  return { id: taskId, title, owner, budgetPoints }
}

const tasks = []
for (let index = 0; index < taskDefinitions.length; index++) {
  tasks.push(await ensureTask(taskDefinitions[index], index))
}

async function ensureTaskSubmission(task, submitter, artwork, decision, rewardPoints) {
  let submissionId = scalar(`SELECT id FROM enterprise_task_submission WHERE task_id=${task.id} AND artwork_id=${artwork.id} LIMIT 1;`)
  if (!submissionId) {
    await api('POST', `/tasks/${task.id}/submissions`, submitter.token, {
      artworkId: artwork.id,
      note: '【演示】用于验证任务投稿、审核、积分奖励与通知流程。'
    })
    submissionId = scalar(`SELECT id FROM enterprise_task_submission WHERE task_id=${task.id} AND artwork_id=${artwork.id} LIMIT 1;`)
  }
  const status = scalar(`SELECT status FROM enterprise_task_submission WHERE id=${submissionId};`)
  if (status === 'PENDING') {
    await api('POST', `/tasks/submissions/${submissionId}/review`, task.owner.token, {
      status: decision,
      comment: decision === 'APPROVED' ? '【演示】符合需求，已发放奖励。' : '【演示】构图方向不符合本轮需求。',
      rewardPoints
    })
  }
}

for (let index = 0; index < 6; index++) {
  const task = tasks[index]
  const submitter = users[(taskDefinitions[index][4] + 1) % users.length]
  await ensureTaskSubmission(task, submitter, submitter.artworks[index % submitter.artworks.length], index === 4 ? 'REJECTED' : 'APPROVED', index === 4 ? 0 : 40 + index * 10)
}

const closeTask = tasks[1]
if (scalar(`SELECT status FROM enterprise_task WHERE id=${closeTask.id};`) === 'PUBLISHED') {
  await api('POST', `/tasks/${closeTask.id}/close`, closeTask.owner.token)
}

const styleCount = scalar(`SELECT COUNT(*) FROM style_package WHERE name LIKE '【演示】%';`)
const taskCount = scalar(`SELECT COUNT(*) FROM enterprise_task WHERE title LIKE '【演示】%';`)
const artworkCount = scalar(`SELECT COUNT(*) FROM artwork WHERE title LIKE '【演示】%';`)
const styleSubmissionCount = scalar(`SELECT COUNT(*) FROM style_package_submission s JOIN style_package p ON p.id=s.style_package_id WHERE p.name LIKE '【演示】%';`)
const taskSubmissionCount = scalar(`SELECT COUNT(*) FROM enterprise_task_submission s JOIN enterprise_task t ON t.id=s.task_id WHERE t.title LIKE '【演示】%';`)

console.log(JSON.stringify({
  demoPassword,
  users: users.map(({ username, displayName }) => ({ username, displayName })),
  counts: {
    users: users.length,
    artworks: Number(artworkCount),
    stylePackages: Number(styleCount),
    styleSubmissions: Number(styleSubmissionCount),
    tasks: Number(taskCount),
    taskSubmissions: Number(taskSubmissionCount)
  }
}, null, 2))
