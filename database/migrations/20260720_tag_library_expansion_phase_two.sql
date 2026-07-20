USE aiart_codex_platform;

INSERT IGNORE INTO tag_category (id, name, slug, parent_id, sort_order) VALUES
  (1010, 'Environment', 'environment', 0, 10),
  (1011, 'Culture', 'culture', 0, 11);

INSERT IGNORE INTO tag (id, category_id, name, display_name_zh, description_zh, prompt_text, negative_prompt_text, preview_image_url, weight, usage_count) VALUES
  (2063, 1010, 'rainy weather', '雨天', '加入降雨、湿润表面、雨雾和阴天环境光。', 'rainy weather, falling rain, wet reflective surfaces, overcast light', 'dry weather, clear sky', '/images/tags/rainy-weather.webp', 1.05, 0),
  (2064, 1010, 'snowy scene', '雪景', '使用积雪、飘雪、冷空气与冬季地表反光塑造环境。', 'snowy scene, fresh snow, falling snowflakes, cold winter atmosphere', 'summer vegetation, warm weather', '/images/tags/snowy-scene.webp', 1.05, 0),
  (2065, 1010, 'foggy atmosphere', '雾天', '通过远景衰减、低对比和雾气层次增加空间深度。', 'foggy atmosphere, layered mist, low contrast distance, atmospheric depth', 'crystal clear distance', '/images/tags/foggy-atmosphere.webp', 1.05, 0),
  (2066, 1010, 'underwater world', '水下世界', '表现水下光束、悬浮颗粒、流动水体与海洋环境。', 'underwater world, caustic light rays, suspended particles, flowing water', 'dry land, open air', '/images/tags/underwater-world.webp', 1.10, 0),
  (2067, 1010, 'outer space', '外太空', '加入星空、行星、失重感和宇宙尺度的深色背景。', 'outer space, distant planets, dense star field, zero-gravity atmosphere', 'daytime earth landscape', '/images/tags/outer-space.webp', 1.10, 0),
  (2068, 1010, 'ancient ruins', '古代遗迹', '使用风化石材、断壁、植物侵蚀和历史痕迹构建遗迹场景。', 'ancient ruins, weathered stone, broken arches, overgrown historical remains', 'modern new construction', '/images/tags/ancient-ruins.webp', 1.05, 0),
  (2069, 1010, 'desert landscape', '沙漠', '表现沙丘、风蚀地貌、热空气和干燥辽阔的空间。', 'desert landscape, sweeping dunes, wind-shaped sand, dry vast horizon', 'lush wet vegetation', '/images/tags/desert-landscape.webp', 1.05, 0),
  (2070, 1010, 'lush forest', '茂密森林', '使用层叠植被、潮湿地表、林间光和丰富绿色层次。', 'lush dense forest, layered vegetation, mossy ground, filtered forest light', 'barren landscape', '/images/tags/lush-forest.webp', 1.05, 0),
  (2071, 1011, 'steampunk', '蒸汽朋克', '融合黄铜机械、蒸汽动力、齿轮和维多利亚工业设计。', 'steampunk aesthetic, brass machinery, steam power, gears, Victorian engineering', NULL, '/images/tags/steampunk.webp', 1.10, 0),
  (2072, 1011, 'art deco', '装饰艺术', '使用几何对称、金属线条、阶梯形纹样和奢华装饰。', 'Art Deco design, geometric symmetry, metallic accents, stepped ornament', NULL, '/images/tags/art-deco.webp', 1.10, 0),
  (2073, 1011, 'art nouveau', '新艺术', '使用植物曲线、花卉纹样、流动轮廓和手工装饰。', 'Art Nouveau style, flowing botanical curves, floral ornament, elegant linework', NULL, '/images/tags/art-nouveau.webp', 1.10, 0),
  (2074, 1011, 'bauhaus', '包豪斯', '强调功能、基础几何、清晰网格和克制的现代配色。', 'Bauhaus design, functional geometry, clear grid, primary color accents', 'ornate decoration', '/images/tags/bauhaus.webp', 1.05, 0),
  (2075, 1011, 'Song dynasty painting', '宋画', '借鉴宋代山水与花鸟画的细腻观察、留白和淡雅设色。', 'Song dynasty Chinese painting, refined brushwork, poetic negative space, muted mineral colors', NULL, '/images/tags/song-dynasty-painting.webp', 1.10, 0),
  (2076, 1011, 'ukiyo-e', '浮世绘', '使用木版套色、平涂色块、明确轮廓和日式传统构图。', 'ukiyo-e woodblock print, flat color areas, bold contours, traditional Japanese composition', NULL, '/images/tags/ukiyo-e.webp', 1.10, 0),
  (2077, 1011, 'Y2K aesthetic', 'Y2K 美学', '使用千禧年科技感、透明塑料、金属色和早期数字界面语言。', 'Y2K aesthetic, translucent plastic, chrome accents, optimistic early-digital design', NULL, '/images/tags/y2k-aesthetic.webp', 1.05, 0),
  (2078, 1011, 'brutalism', '粗野主义', '强调裸露混凝土、厚重体块、结构重复和强烈尺度感。', 'brutalist architecture, exposed concrete, massive geometric volumes, structural repetition', 'delicate ornament', '/images/tags/brutalism.webp', 1.10, 0),
  (2079, 1011, 'rococo', '洛可可', '使用轻盈曲线、贝壳纹、粉彩装饰和精致宫廷细节。', 'Rococo style, ornate shell motifs, pastel decoration, elegant courtly detail', NULL, '/images/tags/rococo.webp', 1.10, 0),
  (2080, 1011, 'synthwave', '合成波', '使用霓虹落日、紫蓝网格、复古未来主义和八十年代电子氛围。', 'synthwave aesthetic, neon sunset, retro grid horizon, 1980s retro-futurism', NULL, '/images/tags/synthwave.webp', 1.10, 0);

INSERT IGNORE INTO tag_preview
  (id, tag_id, image_url, preview_type, scene_key, title_zh, prompt_snapshot, sort_order, is_cover)
SELECT tag.id * 1000 + 1, tag.id, tag.preview_image_url, 'COVER', 'GENERAL',
       CONCAT(COALESCE(tag.display_name_zh, tag.name), '封面'), tag.prompt_text, 0, 1
FROM tag
WHERE tag.id BETWEEN 2063 AND 2080;
