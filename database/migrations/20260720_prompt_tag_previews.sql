UPDATE tag
SET display_name_zh = CASE id
      WHEN 2001 THEN '电影感'
      WHEN 2002 THEN '水彩'
      WHEN 2003 THEN '赛博朋克'
      WHEN 2004 THEN '人物'
      WHEN 2005 THEN '风景'
      WHEN 2006 THEN '柔光'
      WHEN 2007 THEN '轮廓光'
      WHEN 2008 THEN '近景'
      WHEN 2009 THEN '远景'
      WHEN 2010 THEN '高细节'
      ELSE display_name_zh
    END,
    description_zh = CASE id
      WHEN 2001 THEN '强调镜头语言、叙事氛围和胶片质感。'
      WHEN 2002 THEN '偏柔和、带纸面渗色感的插画风格。'
      WHEN 2003 THEN '强调霓虹、未来都市、科技感与反乌托邦氛围。'
      WHEN 2004 THEN '适合角色肖像、半身像和人物主体画面。'
      WHEN 2005 THEN '适合自然景观、城市景观和大场景画面。'
      WHEN 2006 THEN '画面光线更柔和，边缘不过分锐利。'
      WHEN 2007 THEN '强化主体边缘高光和戏剧性光影。'
      WHEN 2008 THEN '突出主体局部细节与面部情绪。'
      WHEN 2009 THEN '适合展现场景空间关系和整体构图。'
      WHEN 2010 THEN '强调清晰度、细节层次和画面完成度。'
      ELSE description_zh
    END,
    preview_image_url = CASE id
      WHEN 2001 THEN '/images/tags/cinematic.webp'
      WHEN 2002 THEN '/images/tags/watercolor.webp'
      WHEN 2003 THEN '/images/tags/cyberpunk.webp'
      WHEN 2004 THEN '/images/tags/character.webp'
      WHEN 2005 THEN '/images/tags/landscape.webp'
      WHEN 2006 THEN '/images/tags/soft-light.webp'
      WHEN 2007 THEN '/images/tags/rim-light.webp'
      WHEN 2008 THEN '/images/tags/close-up.webp'
      WHEN 2009 THEN '/images/tags/wide-shot.webp'
      WHEN 2010 THEN '/images/tags/high-detail.webp'
      ELSE preview_image_url
    END
WHERE id IN (2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010);
