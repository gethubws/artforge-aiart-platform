USE aiart_codex_platform;

INSERT IGNORE INTO tag_preview
  (id, tag_id, image_url, preview_type, scene_key, title_zh, prompt_snapshot, sort_order, is_cover)
VALUES
  (2004002, 2004, '/images/tags/gallery/character/portrait.webp', 'EXAMPLE', 'CHARACTER', '人物肖像示例', 'detailed character portrait', 10, 0),
  (2004003, 2004, '/images/tags/gallery/character/full-body.webp', 'EXAMPLE', 'FULL_BODY', '全身角色示例', 'detailed character portrait', 20, 0),
  (2005002, 2005, '/images/tags/gallery/landscape/nature.webp', 'EXAMPLE', 'LANDSCAPE', '自然风景示例', 'wide landscape scene', 10, 0),
  (2005003, 2005, '/images/tags/gallery/landscape/urban.webp', 'EXAMPLE', 'URBAN', '城市景观示例', 'wide landscape scene', 20, 0),
  (2019002, 2019, '/images/tags/gallery/architecture/exterior.webp', 'EXAMPLE', 'EXTERIOR', '建筑外观示例', 'detailed architecture', 10, 0),
  (2019003, 2019, '/images/tags/gallery/architecture/interior.webp', 'EXAMPLE', 'INTERIOR', '建筑室内示例', 'detailed architecture', 20, 0),
  (2020002, 2020, '/images/tags/gallery/animal/wildlife.webp', 'EXAMPLE', 'WILDLIFE', '野生动物示例', 'detailed animal subject', 10, 0),
  (2020003, 2020, '/images/tags/gallery/animal/domestic.webp', 'EXAMPLE', 'DOMESTIC', '生活动物示例', 'detailed animal subject', 20, 0),
  (2021002, 2021, '/images/tags/gallery/product/studio.webp', 'EXAMPLE', 'STUDIO', '产品棚拍示例', 'professional product photography', 10, 0),
  (2021003, 2021, '/images/tags/gallery/product/lifestyle.webp', 'EXAMPLE', 'LIFESTYLE', '产品生活场景', 'professional product photography', 20, 0),
  (2022002, 2022, '/images/tags/gallery/food/plated.webp', 'EXAMPLE', 'FOOD', '精致摆盘示例', 'appetizing food photography', 10, 0),
  (2022003, 2022, '/images/tags/gallery/food/market.webp', 'EXAMPLE', 'LIFESTYLE', '生活美食示例', 'appetizing food photography', 20, 0),
  (2023002, 2023, '/images/tags/gallery/vehicle/road.webp', 'EXAMPLE', 'ROAD', '道路载具示例', 'detailed vehicle design', 10, 0),
  (2023003, 2023, '/images/tags/gallery/vehicle/studio.webp', 'EXAMPLE', 'STUDIO', '载具设计棚拍', 'detailed vehicle design', 20, 0),
  (2010002, 2010, '/images/tags/gallery/high-detail/close-up.webp', 'COMPARISON', 'CLOSE_UP', '局部高细节示例', 'highly detailed, sharp focus', 10, 0),
  (2010003, 2010, '/images/tags/gallery/high-detail/architecture.webp', 'COMPARISON', 'ARCHITECTURE', '复杂场景高细节', 'highly detailed, sharp focus', 20, 0),
  (2033002, 2033, '/images/tags/gallery/clean-lines/character.webp', 'COMPARISON', 'CHARACTER', '角色线稿示例', 'clean confident linework', 10, 0),
  (2033003, 2033, '/images/tags/gallery/clean-lines/object.webp', 'COMPARISON', 'OBJECT', '产品线稿示例', 'clean confident linework', 20, 0),
  (2034002, 2034, '/images/tags/gallery/professional-color-grading/character.webp', 'COMPARISON', 'CHARACTER', '人物调色示例', 'professional color grading', 10, 0),
  (2034003, 2034, '/images/tags/gallery/professional-color-grading/landscape.webp', 'COMPARISON', 'LANDSCAPE', '风景调色示例', 'professional color grading', 20, 0),
  (2038002, 2038, '/images/tags/gallery/dreamy/character.webp', 'COMPARISON', 'CHARACTER', '梦幻人物示例', 'dreamy ethereal atmosphere', 10, 0),
  (2038003, 2038, '/images/tags/gallery/dreamy/landscape.webp', 'COMPARISON', 'LANDSCAPE', '梦幻场景示例', 'dreamy ethereal atmosphere', 20, 0),
  (2039002, 2039, '/images/tags/gallery/mysterious/character.webp', 'COMPARISON', 'CHARACTER', '神秘人物示例', 'mysterious atmosphere', 10, 0),
  (2039003, 2039, '/images/tags/gallery/mysterious/architecture.webp', 'COMPARISON', 'ARCHITECTURE', '神秘建筑示例', 'mysterious atmosphere', 20, 0),
  (2040002, 2040, '/images/tags/gallery/epic/character.webp', 'COMPARISON', 'CHARACTER', '史诗人物示例', 'epic atmosphere', 10, 0),
  (2040003, 2040, '/images/tags/gallery/epic/landscape.webp', 'COMPARISON', 'LANDSCAPE', '史诗场景示例', 'epic atmosphere', 20, 0);
