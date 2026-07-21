import { readFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const scriptDir = path.dirname(fileURLToPath(import.meta.url))
const rootDir = path.resolve(scriptDir, '..')
const manifestPath = path.join(rootDir, 'frontend', 'public', 'images', 'style-packs', 'emerald-fable', 'manifest.json')
const apiBase = process.env.AIART_API_BASE || 'http://127.0.0.1:8080/api'
const username = process.env.AIART_EMERALD_USER || 'demo_emerald_fable'
const password = process.env.AIART_EMERALD_PASSWORD || 'Demo@2026'

async function rawApi(method, route, token, payload) {
  const response = await fetch(`${apiBase}${route}`, {
    method,
    headers: {
      ...(payload === undefined ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: payload === undefined ? undefined : JSON.stringify(payload)
  })
  const body = await response.json()
  if (!response.ok || body.code !== 0) {
    throw new Error(`${method} ${route}: ${body.message || response.statusText}`)
  }
  return body.data
}

async function ensureLogin() {
  try {
    return await rawApi('POST', '/auth/login', '', { username, password })
  } catch {
    await rawApi('POST', '/auth/register', '', {
      username,
      password,
      displayName: '翡翠森林资源组'
    })
    return rawApi('POST', '/auth/login', '', { username, password })
  }
}

const manifest = JSON.parse(await readFile(manifestPath, 'utf8'))
const login = await ensureLogin()
const token = login.token
const packagePayload = {
  name: '翡翠童话森林 · 游戏美术资源包',
  description: '面向独立游戏、互动叙事与视觉原型的统一风格资源包，包含植被、建筑、道具、角色、生物、地形、UI 与特效资源。',
  coverImageUrl: '/images/style-packs/emerald-fable/cover.png',
  styleStatement: '以翡翠绿、浅金和少量珊瑚色建立童话森林视觉语言，强调圆润轮廓、手绘材质和清晰的游戏可读性。所有资源可组合成同一世界观中的完整场景。',
  promptGuide: 'Emerald Fable Forest, stylized hand-painted 3D fantasy game asset, jade green, pale gold trim, restrained coral accents, soft diffuse light, rounded readable silhouette, premium indie game art',
  negativePromptGuide: 'photorealistic, gritty realism, horror, muddy colors, flat silhouette, text, watermark, logo, inconsistent art direction',
  licenseType: 'STANDARD',
  licenseSummary: '兑换后可将包内资源用于个人与商业项目，可修改和二次创作；不得将原始资源或轻微修改后的资源单独转售、共享或重新打包分发。',
  commercialUse: true,
  featuredArtworkId: null,
  pricePoints: 180,
  tagNames: ['fantasy art', 'lush forest', 'soft light', 'high detail', '3d render'],
  collaborators: []
}

const mine = await rawApi('GET', `/style-packages/my?keyword=${encodeURIComponent(packagePayload.name)}&size=60`, token)
let stylePackage = mine.items.find((item) => item.name === packagePayload.name)
if (!stylePackage) {
  stylePackage = await rawApi('POST', '/style-packages', token, packagePayload)
}

const existingAssets = await rawApi('GET', `/style-packages/${stylePackage.id}/assets`, token)
const existingKeys = new Set(existingAssets.map((asset) => asset.logicalKey))
const assetsToCreate = []

for (let index = 0; index < manifest.assets.length; index += 1) {
  const asset = manifest.assets[index]
  const logicalKey = `emerald-fable-${asset.filename.replace(/\.png$/i, '')}`
  if (existingKeys.has(logicalKey)) continue
  const imageUrl = `/images/style-packs/emerald-fable/${asset.filename}`
  assetsToCreate.push({
    logicalKey,
    name: asset.name,
    categoryKey: asset.categoryKey,
    assetType: 'IMAGE',
    description: `${asset.name}，属于“翡翠童话森林”统一美术方向，可与包内其他资源直接组合使用。`,
    previewImageUrl: imageUrl,
    fileUrl: imageUrl,
    thumbnailUrl: imageUrl,
    promptText: asset.promptText,
    negativePromptText: packagePayload.negativePromptGuide,
    generationParamsJson: JSON.stringify({ model: 'gpt-image-2', source: 'emerald-fable-generator', sheet: asset.filename.split('-')[0] }),
    width: asset.width,
    height: asset.height,
    fileFormat: asset.fileFormat,
    backgroundMode: asset.backgroundMode,
    licenseScope: 'PACKAGE',
    sortOrder: index
  })
}

if (assetsToCreate.length) {
  await rawApi('POST', `/style-packages/${stylePackage.id}/assets/batch`, token, { assets: assetsToCreate })
}

const detail = await rawApi('GET', `/style-packages/${stylePackage.id}`, token)
if (detail.status !== 'PUBLISHED') {
  await rawApi('POST', `/style-packages/${stylePackage.id}/publish`, token)
}

console.log(JSON.stringify({
  packageId: stylePackage.id,
  packageName: packagePayload.name,
  manifestAssets: manifest.assets.length,
  newlyCreatedAssets: assetsToCreate.length
}, null, 2))
