USE aiart_codex_platform;

INSERT IGNORE INTO tag_category (id, name, slug, parent_id, sort_order) VALUES
  (1008, 'Camera', 'camera', 0, 8),
  (1009, 'Material', 'material', 0, 9);

INSERT IGNORE INTO tag (id, category_id, name, display_name_zh, description_zh, prompt_text, negative_prompt_text, preview_image_url, weight, usage_count) VALUES
  (2041, 1001, 'engraving', '版画', '通过密集排线、刻痕和黑白层次形成传统版画质感。', 'black ink engraving, dense cross-hatching, carved line texture', NULL, '/images/tags/engraving.webp', 1.10, 0),
  (2042, 1001, 'pencil sketch', '铅笔素描', '使用石墨线条、轻重排线和纸张颗粒表现手绘结构。', 'graphite pencil sketch, visible construction lines, paper grain', NULL, '/images/tags/pencil-sketch.webp', 1.05, 0),
  (2043, 1001, 'cel shading', '赛璐璐上色', '使用清晰轮廓、分区阴影和动画式平涂色块。', 'cel-shaded illustration, crisp outlines, flat color blocks', NULL, '/images/tags/cel-shading.webp', 1.05, 0),
  (2044, 1001, 'claymation', '黏土动画', '呈现手工黏土塑形、轻微指纹和定格动画质感。', 'claymation style, handcrafted clay figures, stop-motion texture', NULL, '/images/tags/claymation.webp', 1.10, 0),
  (2045, 1001, 'low poly', '低多边形', '使用明确多边形切面、简化造型和几何明暗关系。', 'low-poly 3D art, faceted geometry, simplified forms', NULL, '/images/tags/low-poly.webp', 1.05, 0),
  (2046, 1001, 'paper cut', '纸艺剪纸', '使用分层纸片、清晰剪裁边缘和手工阴影表现画面。', 'layered paper-cut art, handcrafted paper edges, cut-paper depth', NULL, '/images/tags/paper-cut.webp', 1.10, 0),
  (2047, 1001, 'retro poster', '复古海报', '使用有限色板、粗颗粒印刷和复古图形设计语言。', 'retro poster illustration, limited color palette, screen-print texture', NULL, '/images/tags/retro-poster.webp', 1.05, 0),
  (2048, 1001, 'manga line art', '漫画线稿', '强调黑白墨线、速度线、网点和清晰角色轮廓。', 'manga line art, expressive ink lines, screentone shading', NULL, '/images/tags/manga-line-art.webp', 1.10, 0),
  (2049, 1008, 'macro photography', '微距摄影', '极近距离呈现微小主体与材质细节，背景通常明显虚化。', 'macro photography, extreme close focus, fine surface detail', NULL, '/images/tags/macro-photography.webp', 1.10, 0),
  (2050, 1008, 'shallow depth of field', '浅景深', '让主体清晰、前后景柔和虚化，突出视觉焦点。', 'shallow depth of field, sharp subject, softly blurred background', NULL, '/images/tags/shallow-depth-of-field.webp', 1.05, 0),
  (2051, 1008, 'wide-angle lens', '广角镜头', '扩大空间感和透视关系，适合建筑、室内与宏大环境。', 'wide-angle lens, expansive perspective, strong spatial depth', 'telephoto compression', '/images/tags/wide-angle-lens.webp', 1.05, 0),
  (2052, 1008, 'telephoto lens', '长焦镜头', '压缩远近距离并突出远处主体，形成平整的空间层次。', 'telephoto lens, compressed perspective, isolated distant subject', 'wide-angle distortion', '/images/tags/telephoto-lens.webp', 1.05, 0),
  (2053, 1008, 'fisheye lens', '鱼眼镜头', '使用强烈桶形畸变和超广视野营造夸张动感。', 'fisheye lens, dramatic barrel distortion, ultra-wide field of view', NULL, '/images/tags/fisheye-lens.webp', 1.05, 0),
  (2054, 1008, 'tilt-shift photography', '移轴摄影', '通过选择性焦平面与俯视构图产生微缩模型般的效果。', 'tilt-shift photography, selective focus plane, miniature effect', NULL, '/images/tags/tilt-shift-photography.webp', 1.05, 0),
  (2055, 1008, 'motion blur', '动态模糊', '保留运动方向上的拖影，表达速度、移动与追随拍摄感。', 'intentional motion blur, directional movement streaks, sense of speed', 'static frozen motion', '/images/tags/motion-blur.webp', 1.05, 0),
  (2056, 1009, 'glass material', '玻璃材质', '强调透明折射、边缘高光、内部反射与真实厚度。', 'realistic glass material, refraction, edge highlights, optical thickness', NULL, '/images/tags/glass-material.webp', 1.10, 0),
  (2057, 1009, 'metallic material', '金属材质', '强调金属反射、拉丝或抛光表面以及环境映射。', 'realistic metallic material, polished reflections, brushed metal detail', NULL, '/images/tags/metallic-material.webp', 1.10, 0),
  (2058, 1009, 'ceramic material', '陶瓷材质', '表现釉面高光、烧制纹理、细小瑕疵与厚实质感。', 'glazed ceramic material, fired surface texture, subtle imperfections', NULL, '/images/tags/ceramic-material.webp', 1.05, 0),
  (2059, 1009, 'fabric texture', '织物纹理', '突出纤维、编织结构、褶皱和柔软表面。', 'detailed fabric texture, visible fibers, woven structure, soft folds', NULL, '/images/tags/fabric-texture.webp', 1.05, 0),
  (2060, 1009, 'wood texture', '木材纹理', '表现年轮、木纹方向、雕刻痕迹和自然表面变化。', 'natural wood texture, visible grain, carved detail, organic variation', NULL, '/images/tags/wood-texture.webp', 1.05, 0),
  (2061, 1009, 'translucent material', '半透明材质', '允许部分光线穿透，呈现柔和内部散射与层次。', 'translucent material, subsurface light scattering, soft internal glow', NULL, '/images/tags/translucent-material.webp', 1.10, 0),
  (2062, 1009, 'frosted material', '磨砂材质', '使用细腻粗糙表面散射光线，形成柔和朦胧的透光感。', 'frosted material, diffused transmission, fine matte surface', NULL, '/images/tags/frosted-material.webp', 1.05, 0);

INSERT IGNORE INTO tag_preview
  (id, tag_id, image_url, preview_type, scene_key, title_zh, prompt_snapshot, sort_order, is_cover)
SELECT
  tag.id * 1000 + 1,
  tag.id,
  tag.preview_image_url,
  'COVER',
  'GENERAL',
  CONCAT(COALESCE(tag.display_name_zh, tag.name), '封面'),
  tag.prompt_text,
  0,
  1
FROM tag
WHERE tag.id BETWEEN 2041 AND 2062;
