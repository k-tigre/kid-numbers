"""Pillow-based PNG renderer (fallback when cairosvg is unavailable)."""
from __future__ import annotations
import shutil
import textwrap
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter, ImageFont

MARKETING_SCREEN_CROP: tuple[int, int, int, int] = (106, 296, 974, 1844)

UAC_LAYOUT = {
    "text_left": 60,
    "text_max_width": 580,
    "phone_outer": (700, 80, 1120, 548),
    "phone_screen": (720, 100, 1100, 528),
}


def brand_name(brand: dict, locale: str) -> str:
    key: str = f"name{locale.capitalize()}"
    return brand.get(key, brand.get("nameEn", "Numbers"))


def feature_title(feature_graphic: dict, locale: str) -> str:
    key: str = f"title{locale.capitalize()}"
    return feature_graphic.get(key, feature_graphic.get("titleEn", feature_graphic.get("title", "Numbers")))


def banner_tagline(locale: str) -> str:
    if locale == "ru":
        return "Бесплатно · Без рекламы · RU/EN"
    return "Free · No ads · RU/EN"


PHONE = {
    "x": 90,
    "y": 280,
    "width": 900,
    "height": 1580,
    "bezel": 16,
    "screen_radius": 36,
    "radius": 48,
}

FEATURE_GRAPHIC = {
    "footer_y": 420,
    "icon_x": 72,
    "icon_y": 100,
    "icon_size": 200,
    "text_x": 296,
    "title_y": 134,
    "subtitle_y": 242,
}

FEATURE_DECOR_CIRCLES: list[tuple[int, int, int, float]] = [
    (900, 150, 150, 0.14),
    (960, 210, 105, 0.22),
    (840, 230, 75, 0.30),
]


def hex_to_rgb(hex_color: str) -> tuple[int, int, int]:
    value: str = hex_color.lstrip("#")
    if len(value) == 8:
        value = value[:6]
    return tuple(int(value[i : i + 2], 16) for i in (0, 2, 4))


def blend_rgb(
    background: tuple[int, int, int],
    foreground: tuple[int, int, int],
    alpha: float,
) -> tuple[int, int, int]:
    return tuple(
        int(background[channel] * (1.0 - alpha) + foreground[channel] * alpha)
        for channel in range(3)
    )


def playstore_icon_path(assets_dir: Path) -> Path:
    repo_root: Path = assets_dir.parent.parent.parent
    return repo_root / "androidApp" / "src" / "main" / "ic_launcher-playstore.png"


def render_icon_png_from_svg(svg_path: Path, png_path: Path, size: int = 512) -> bool:
    try:
        import cairosvg
    except ImportError:
        return False
    png_path.parent.mkdir(parents=True, exist_ok=True)
    cairosvg.svg2png(
        url=str(svg_path.resolve()),
        write_to=str(png_path.resolve()),
        output_width=size,
        output_height=size,
    )
    return png_path.exists()


def draw_launcher_icon_fallback(size: int) -> Image.Image:
    canvas: Image.Image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw: ImageDraw.ImageDraw = ImageDraw.Draw(canvas)
    corner_radius: int = round(size * 24 / 108)
    draw.rounded_rectangle((0, 0, size - 1, size - 1), radius=corner_radius, fill=(255, 255, 255, 255))
    gray: tuple[int, int, int] = (128, 129, 132)
    inset: int = round(size * 20.8 / 108)
    grid_size: int = size - inset * 2
    cell: int = grid_size // 2
    gap: int = round(size * 2 / 108)
    for row in range(2):
        for col in range(2):
            x0: int = inset + col * (cell + gap)
            y0: int = inset + row * (cell + gap)
            draw.rounded_rectangle(
                (x0, y0, x0 + cell - gap, y0 + cell - gap),
                radius=round(size * 2 / 108),
                outline=gray,
                width=max(4, round(size * 2.5 / 108)),
            )
    stroke: int = max(6, round(size * 5 / 108))
    plus_cx: int = inset + (cell - gap) // 2
    plus_cy: int = inset + (cell - gap) // 2
    plus_arm: int = round(size * 10 / 108)
    draw.line((plus_cx - plus_arm, plus_cy, plus_cx + plus_arm, plus_cy), fill=gray, width=stroke)
    draw.line((plus_cx, plus_cy - plus_arm, plus_cx, plus_cy + plus_arm), fill=gray, width=stroke)
    minus_x0: int = inset + cell + gap + round((cell - gap) * 0.2)
    minus_x1: int = inset + cell + gap + round((cell - gap) * 0.8)
    minus_y1: int = inset + (cell - gap) // 2 - round(size * 1.5 / 108)
    minus_y2: int = inset + (cell - gap) // 2 + round(size * 1.5 / 108)
    draw.line((minus_x0, minus_y1, minus_x1, minus_y1), fill=gray, width=stroke)
    draw.line((minus_x0, minus_y2, minus_x1, minus_y2), fill=gray, width=stroke)
    mult_cx: int = inset + (cell - gap) // 2
    mult_cy: int = inset + cell + gap + (cell - gap) // 2
    mult_arm: int = round(size * 8 / 108)
    for dx, dy in [(-mult_arm, -mult_arm), (mult_arm, mult_arm), (-mult_arm, mult_arm), (mult_arm, -mult_arm)]:
        draw.line(
            (mult_cx + dx, mult_cy + dy, mult_cx - dx, mult_cy - dy),
            fill=gray,
            width=max(4, round(size * 4 / 108)),
        )
    equals_x0: int = inset + cell + gap + round((cell - gap) * 0.2)
    equals_x1: int = inset + cell + gap + round((cell - gap) * 0.8)
    equals_cy: int = inset + cell + gap + (cell - gap) // 2
    equals_gap: int = round(size * 3 / 108)
    draw.line(
        (equals_x0, equals_cy - equals_gap, equals_x1, equals_cy - equals_gap),
        fill=gray,
        width=stroke,
    )
    draw.line(
        (equals_x0, equals_cy + equals_gap, equals_x1, equals_cy + equals_gap),
        fill=gray,
        width=stroke,
    )
    return canvas


def ensure_icon_png(sources_dir: Path) -> Path:
    png_path: Path = sources_dir / "icon.png"
    playstore_icon: Path = playstore_icon_path(sources_dir)
    if playstore_icon.exists():
        if not png_path.exists() or png_path.stat().st_mtime < playstore_icon.stat().st_mtime:
            png_path.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(playstore_icon, png_path)
        return png_path
    svg_path: Path = sources_dir / "icon.svg"
    if svg_path.exists():
        if not png_path.exists() or png_path.stat().st_mtime < svg_path.stat().st_mtime:
            if not render_icon_png_from_svg(svg_path, png_path):
                canvas: Image.Image = draw_launcher_icon_fallback(512)
                png_path.parent.mkdir(parents=True, exist_ok=True)
                canvas.save(png_path, "PNG")
    elif not png_path.exists():
        canvas = draw_launcher_icon_fallback(512)
        png_path.parent.mkdir(parents=True, exist_ok=True)
        canvas.save(png_path, "PNG")
    return png_path


def paste_app_icon(
    canvas: Image.Image,
    icon_path: Path,
    x: int,
    y: int,
    size: int,
) -> None:
    source: Image.Image = Image.open(icon_path).convert("RGBA")
    corner_radius: int = round(size * 22 / 108)
    inner_scale: float = 0.80
    inner_size: int = round(size * inner_scale)
    inner_offset: int = (size - inner_size) // 2
    tile: Image.Image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    tile_draw: ImageDraw.ImageDraw = ImageDraw.Draw(tile)
    tile_draw.rounded_rectangle((0, 0, size - 1, size - 1), radius=corner_radius, fill=(255, 255, 255, 255))
    inner_icon: Image.Image = source.resize((inner_size, inner_size), Image.Resampling.LANCZOS)
    tile.paste(inner_icon, (inner_offset, inner_offset), inner_icon)
    mask: Image.Image = Image.new("L", (size, size), 0)
    mask_draw: ImageDraw.ImageDraw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle((0, 0, size - 1, size - 1), radius=corner_radius, fill=255)
    rounded_icon: Image.Image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    rounded_icon.paste(tile, (0, 0), mask)
    shadow_layer: Image.Image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    shadow_draw: ImageDraw.ImageDraw = ImageDraw.Draw(shadow_layer)
    shadow_draw.rounded_rectangle((0, 0, size - 1, size - 1), radius=corner_radius, fill=(0, 0, 0, 14))
    shadow: Image.Image = shadow_layer.filter(ImageFilter.GaussianBlur(radius=3))
    base: Image.Image = canvas.convert("RGBA")
    base.paste(shadow, (x + 1, y + 2), shadow)
    base.paste(rounded_icon, (x, y), rounded_icon)
    canvas.paste(base.convert("RGB"))


def draw_feature_graphic_decor(
    draw: ImageDraw.ImageDraw,
    background: tuple[int, int, int],
    accent: tuple[int, int, int],
) -> None:
    for center_x, center_y, radius, alpha in FEATURE_DECOR_CIRCLES:
        fill: tuple[int, int, int] = blend_rgb(background, accent, alpha)
        draw.ellipse(
            (center_x - radius, center_y - radius, center_x + radius, center_y + radius),
            fill=fill,
        )


def load_font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates: list[str] = []
    if bold:
        candidates.extend(["arialbd.ttf", "segoeuib.ttf", "calibrib.ttf"])
    else:
        candidates.extend(["arial.ttf", "segoeui.ttf", "calibri.ttf"])
    for name in candidates:
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def wrap_text(text: str, max_chars: int = 28) -> list[str]:
    if "\n" in text:
        manual_lines: list[str] = [line.strip() for line in text.split("\n") if line.strip()]
        return manual_lines if manual_lines else [text]
    lines: list[str] = textwrap.wrap(text, width=max_chars)
    if not lines:
        return [text]
    if len(lines) == 2 and len(lines[1].split()) == 1:
        words: list[str] = text.split()
        if len(words) >= 3:
            first_line: str = " ".join(words[:-2])
            second_line: str = " ".join(words[-2:])
            if len(first_line) <= max_chars + 8 and len(second_line) <= max_chars:
                return [first_line, second_line]
    return lines


def rounded_rectangle(
    draw: ImageDraw.ImageDraw,
    xy: tuple[int, int, int, int],
    radius: int,
    fill: tuple[int, int, int] | None = None,
    outline: tuple[int, int, int] | None = None,
    width: int = 1,
) -> None:
    draw.rounded_rectangle(xy, radius=radius, fill=fill, outline=outline, width=width)


def draw_centered_text(
    draw: ImageDraw.ImageDraw,
    text: str,
    center_x: int,
    y: int,
    font: ImageFont.FreeTypeFont | ImageFont.ImageFont,
    fill: tuple[int, int, int],
) -> None:
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width: int = bbox[2] - bbox[0]
    draw.text((center_x - text_width // 2, y), text, font=font, fill=fill)


def text_width(draw: ImageDraw.ImageDraw, text: str, font: ImageFont.FreeTypeFont | ImageFont.ImageFont) -> int:
    bbox: tuple[int, int, int, int] = draw.textbbox((0, 0), text, font=font)
    return bbox[2] - bbox[0]


def fit_font(
    draw: ImageDraw.ImageDraw,
    text: str,
    max_width: int,
    start_size: int,
    min_size: int,
    bold: bool = False,
) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    for size in range(start_size, min_size - 1, -2):
        font: ImageFont.FreeTypeFont | ImageFont.ImageFont = load_font(size, bold)
        if text_width(draw, text, font) <= max_width:
            return font
    return load_font(min_size, bold)


def crop_marketing_screenshot(image_path: Path) -> Image.Image:
    image: Image.Image = Image.open(image_path).convert("RGBA")
    left, top, right, bottom = MARKETING_SCREEN_CROP
    return image.crop((left, top, right, bottom))


def resolve_uac_screenshot(
    screenshots_dir: Path,
    output_dir: Path,
    locale: str,
    screenshot_file: str,
) -> Image.Image | None:
    raw_path: Path = screenshots_dir / locale / screenshot_file
    if raw_path.exists():
        return Image.open(raw_path).convert("RGBA")
    marketing_path: Path = output_dir / "screenshots" / locale / screenshot_file
    if marketing_path.exists():
        return crop_marketing_screenshot(marketing_path)
    return None


def paste_screenshot_image(
    canvas: Image.Image,
    screenshot: Image.Image,
    screen_box: tuple[int, int, int, int],
    radius: int,
) -> None:
    screen_w: int = screen_box[2] - screen_box[0]
    screen_h: int = screen_box[3] - screen_box[1]
    screenshot_ratio: float = screenshot.width / screenshot.height
    target_ratio: float = screen_w / screen_h
    cropped: Image.Image = screenshot
    if screenshot_ratio > target_ratio:
        new_height: int = screenshot.height
        new_width: int = int(new_height * target_ratio)
        left: int = (screenshot.width - new_width) // 2
        cropped = screenshot.crop((left, 0, left + new_width, new_height))
    else:
        new_width = screenshot.width
        new_height = int(new_width / target_ratio)
        top: int = (screenshot.height - new_height) // 2
        cropped = screenshot.crop((0, top, new_width, top + new_height))
    resized: Image.Image = cropped.resize((screen_w, screen_h), Image.Resampling.LANCZOS)
    mask: Image.Image = Image.new("L", (screen_w, screen_h), 0)
    mask_draw: ImageDraw.ImageDraw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle((0, 0, screen_w, screen_h), radius=radius, fill=255)
    canvas.paste(resized, (screen_box[0], screen_box[1]), mask)


def paste_screenshot(
    canvas: Image.Image,
    screenshot_path: Path,
    screen_box: tuple[int, int, int, int],
    radius: int,
) -> None:
    screenshot: Image.Image = Image.open(screenshot_path).convert("RGBA")
    paste_screenshot_image(canvas, screenshot, screen_box, radius)


def draw_placeholder(
    draw: ImageDraw.ImageDraw,
    screen_box: tuple[int, int, int, int],
    label: str,
    text_color: tuple[int, int, int],
    fill_color: tuple[int, int, int],
) -> None:
    rounded_rectangle(draw, screen_box, radius=8, fill=fill_color, outline=text_color, width=3)
    center_x: int = (screen_box[0] + screen_box[2]) // 2
    center_y: int = (screen_box[1] + screen_box[3]) // 2
    font_main: ImageFont.FreeTypeFont | ImageFont.ImageFont = load_font(36)
    font_sub: ImageFont.FreeTypeFont | ImageFont.ImageFont = load_font(28)
    draw_centered_text(draw, "Вставьте скриншот", center_x, center_y - 40, font_main, text_color)
    draw_centered_text(draw, label, center_x, center_y + 10, font_sub, text_color)


def draw_phone_frame(
    canvas: Image.Image,
    draw: ImageDraw.ImageDraw,
    brand: dict,
    screenshot_file: str,
    screenshots_dir: Path,
    is_banner: bool,
    app_name: str,
    banner_tagline_text: str,
) -> None:
    px: int = PHONE["x"]
    py: int = PHONE["y"]
    pw: int = PHONE["width"]
    ph: int = PHONE["height"]
    bezel: int = PHONE["bezel"]
    frame_color: tuple[int, int, int] = hex_to_rgb(brand["phoneFrame"])
    accent: tuple[int, int, int] = hex_to_rgb(brand["accent"])
    text_color: tuple[int, int, int] = hex_to_rgb(brand["text"])
    accent_dark: tuple[int, int, int] = hex_to_rgb(brand["accentDark"])
    shadow: Image.Image = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    shadow_draw: ImageDraw.ImageDraw = ImageDraw.Draw(shadow)
    shadow_draw.rounded_rectangle(
        (px + 4, py + 12, px + pw + 4, py + ph + 12),
        radius=PHONE["radius"],
        fill=(0, 0, 0, 38),
    )
    canvas.alpha_composite(shadow)
    rounded_rectangle(draw, (px, py, px + pw, py + ph), radius=PHONE["radius"], fill=frame_color)
    screen_box: tuple[int, int, int, int] = (
        px + bezel,
        py + bezel,
        px + pw - bezel,
        py + ph - bezel,
    )
    rounded_rectangle(draw, screen_box, radius=PHONE["screen_radius"], fill=(255, 255, 255))
    screenshot_path: Path = screenshots_dir / screenshot_file
    if screenshot_path.exists():
        paste_screenshot(canvas, screenshot_path, screen_box, PHONE["screen_radius"])
    elif is_banner:
        rounded_rectangle(draw, screen_box, radius=PHONE["screen_radius"], fill=accent)
        draw_centered_text(draw, app_name, (screen_box[0] + screen_box[2]) // 2, screen_box[1] + screen_box[3] // 2 - 60, load_font(72, True), accent_dark)
        draw_centered_text(draw, banner_tagline_text, (screen_box[0] + screen_box[2]) // 2, screen_box[1] + screen_box[3] // 2 + 30, load_font(40), text_color)
    else:
        draw_placeholder(draw, screen_box, screenshot_file, hex_to_rgb("#747878"), hex_to_rgb("#F1EDEC"))


def caption_accent_y(
    line_count: int,
    start_y: int,
    line_height: int,
    last_line: str,
    font: ImageFont.FreeTypeFont | ImageFont.ImageFont,
) -> int:
    if line_count == 1:
        return 200
    bbox: tuple[int, int, int, int] = font.getbbox(last_line)
    text_height: int = bbox[3] - bbox[1]
    last_line_top: int = start_y + (line_count - 1) * line_height
    return last_line_top + text_height + 14


def render_screenshot_png(
    caption: str,
    screenshot_file: str,
    brand: dict,
    screenshots_dir: Path,
    output_path: Path,
    is_banner: bool = False,
    app_name: str = "Numbers",
    banner_tagline_text: str = "Free · No ads · RU/EN",
) -> None:
    background: tuple[int, int, int] = hex_to_rgb(brand["background"])
    text_color: tuple[int, int, int] = hex_to_rgb(brand["text"])
    accent: tuple[int, int, int] = hex_to_rgb(brand["accent"])
    canvas: Image.Image = Image.new("RGBA", (1080, 1920), background + (255,))
    draw: ImageDraw.ImageDraw = ImageDraw.Draw(canvas)
    lines: list[str] = wrap_text(caption)
    caption_font: ImageFont.FreeTypeFont | ImageFont.ImageFont = load_font(48, True)
    line_height: int = 58
    start_y: int = 120 + (2 - len(lines)) * 20
    for index, line in enumerate(lines):
        draw_centered_text(draw, line, 540, start_y + index * line_height, caption_font, text_color)
    accent_width: int = min(len(caption) * 14, 400)
    accent_y: int = caption_accent_y(len(lines), start_y, line_height, lines[-1], caption_font)
    draw.rounded_rectangle(
        (540 - accent_width // 2, accent_y, 540 + accent_width // 2, accent_y + 6),
        radius=3,
        fill=accent,
    )
    draw_phone_frame(
        canvas,
        draw,
        brand,
        screenshot_file,
        screenshots_dir,
        is_banner,
        app_name,
        banner_tagline_text,
    )
    output_path.parent.mkdir(parents=True, exist_ok=True)
    temp_path: Path = output_path.with_suffix(".tmp.png")
    canvas.convert("RGB").save(temp_path, "PNG")
    temp_path.replace(output_path)


def render_feature_graphic_png(
    title: str,
    subtitle: str,
    badge: str,
    brand: dict,
    icon_path: Path,
    output_path: Path,
) -> None:
    layout: dict = FEATURE_GRAPHIC
    background: tuple[int, int, int] = hex_to_rgb(brand["background"])
    text_color: tuple[int, int, int] = hex_to_rgb(brand["text"])
    accent: tuple[int, int, int] = hex_to_rgb(brand["accent"])
    accent_dark: tuple[int, int, int] = hex_to_rgb(brand["accentDark"])
    canvas: Image.Image = Image.new("RGB", (1024, 500), background)
    draw: ImageDraw.ImageDraw = ImageDraw.Draw(canvas)
    draw_feature_graphic_decor(draw, background, accent)
    draw.rectangle((0, layout["footer_y"], 1024, 500), fill=accent)
    draw_centered_text(draw, badge, 512, 438, load_font(32, True), accent_dark)
    if icon_path.exists():
        paste_app_icon(canvas, icon_path, layout["icon_x"], layout["icon_y"], layout["icon_size"])
    draw.text((layout["text_x"], layout["title_y"]), title, font=load_font(96, True), fill=text_color)
    draw.text((layout["text_x"], layout["subtitle_y"]), subtitle, font=load_font(44), fill=text_color)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(output_path, "PNG")


def uac_game_screenshot_path(screenshots_dir: Path, locale: str) -> Path | None:
    for name in ("02-game-timer.png", "03-feedback.png", "01-main-menu.png"):
        candidate: Path = screenshots_dir / locale / name
        if candidate.exists():
            return candidate
    return None


def render_uac_banner_png(
    headline: str,
    subtitle: str,
    cta: str,
    brand: dict,
    icon_path: Path,
    output_path: Path,
    screenshots_dir: Path | None = None,
    output_dir: Path | None = None,
    locale: str = "ru",
    screenshot_file: str = "02-game-timer.png",
) -> None:
    layout: dict = UAC_LAYOUT
    background: tuple[int, int, int] = hex_to_rgb(brand["background"])
    text_color: tuple[int, int, int] = hex_to_rgb(brand["text"])
    accent: tuple[int, int, int] = hex_to_rgb(brand["accent"])
    accent_dark: tuple[int, int, int] = hex_to_rgb(brand["accentDark"])
    frame_color: tuple[int, int, int] = hex_to_rgb(brand["phoneFrame"])
    canvas: Image.Image = Image.new("RGB", (1200, 628), background)
    draw: ImageDraw.ImageDraw = ImageDraw.Draw(canvas)
    draw.rectangle((0, 0, 12, 628), fill=accent)
    if icon_path.exists():
        icon: Image.Image = Image.open(icon_path).convert("RGBA")
        icon = icon.resize((100, 100), Image.Resampling.LANCZOS)
        canvas.paste(icon, (layout["text_left"], 60), icon)
    headline_start: int = 96 if len(headline) <= 12 else 64
    headline_font: ImageFont.FreeTypeFont | ImageFont.ImageFont = fit_font(
        draw, headline, layout["text_max_width"], headline_start, 40, bold=True
    )
    draw.text((layout["text_left"], 170), headline, font=headline_font, fill=text_color)
    subtitle_font: ImageFont.FreeTypeFont | ImageFont.ImageFont = fit_font(
        draw, subtitle, layout["text_max_width"], 36, 24
    )
    draw.text((layout["text_left"], 270), subtitle, font=subtitle_font, fill=text_color)
    cta_font: ImageFont.FreeTypeFont | ImageFont.ImageFont = load_font(32, True)
    cta_width: int = text_width(draw, cta, cta_font) + 64
    cta_width = max(cta_width, 200)
    cta_left: int = layout["text_left"]
    cta_right: int = cta_left + cta_width
    draw.rounded_rectangle((cta_left, 380, cta_right, 452), radius=36, fill=accent)
    draw_centered_text(draw, cta, cta_left + cta_width // 2, 396, cta_font, accent_dark)
    phone_outer: tuple[int, int, int, int] = layout["phone_outer"]
    rounded_rectangle(draw, phone_outer, radius=32, fill=frame_color)
    screen_box: tuple[int, int, int, int] = layout["phone_screen"]
    screenshot_image: Image.Image | None = None
    if screenshots_dir and output_dir:
        screenshot_image = resolve_uac_screenshot(screenshots_dir, output_dir, locale, screenshot_file)
    elif screenshots_dir:
        raw_path: Path | None = uac_game_screenshot_path(screenshots_dir, locale)
        if raw_path:
            screenshot_image = Image.open(raw_path).convert("RGBA")
    if screenshot_image:
        paste_screenshot_image(canvas, screenshot_image, screen_box, radius=24)
    else:
        rounded_rectangle(draw, screen_box, radius=24, fill=(255, 255, 255), outline=hex_to_rgb("#C4C7C7"), width=2)
        draw_centered_text(draw, "скриншот игры", 910, 290, load_font(28), hex_to_rgb("#747878"))
        draw_centered_text(draw, "(вставить в Figma)", 910, 330, load_font(22), hex_to_rgb("#747878"))
    output_path.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(output_path, "PNG")


def render_all_from_config(
    config: dict,
    sources_dir: Path,
    screenshots_dir: Path,
    output_dir: Path,
) -> list[Path]:
    brand: dict = config["brand"]
    icon_path: Path = ensure_icon_png(sources_dir)
    rendered: list[Path] = []
    for locale, caption_key in [("ru", "captionRu"), ("en", "captionEn")]:
        app_name: str = brand_name(brand, locale)
        tagline: str = banner_tagline(locale)
        for item in config["screenshots"]:
            if item.get("disabled"):
                continue
            output_path: Path = output_dir / "screenshots" / locale / f"{item['id']}.png"
            render_screenshot_png(
                caption=item[caption_key],
                screenshot_file=f"{locale}/{item['file']}",
                brand=brand,
                screenshots_dir=screenshots_dir,
                output_path=output_path,
                is_banner=item.get("isBanner", False),
                app_name=app_name,
                banner_tagline_text=tagline,
            )
            rendered.append(output_path)
    fg: dict = config["featureGraphic"]
    for locale, subtitle_key, badge_key in [
        ("ru", "subtitleRu", "badgeRu"),
        ("en", "subtitleEn", "badgeEn"),
    ]:
        output_path = output_dir / "feature-graphic" / f"feature-graphic-{locale}.png"
        render_feature_graphic_png(
            title=feature_title(fg, locale),
            subtitle=fg[subtitle_key],
            badge=fg[badge_key],
            brand=brand,
            icon_path=icon_path,
            output_path=output_path,
        )
        rendered.append(output_path)
    for index, banner in enumerate(config["uacBanners"], start=1):
        for locale, subtitle_key, cta_key in [
            ("ru", "subtitleRu", "ctaRu"),
            ("en", "subtitleEn", "ctaEn"),
        ]:
            headline_key: str = "headline" if "headline" in banner else f"headline{locale.capitalize()}"
            output_path = output_dir / "uac-banners" / f"uac-{index:02d}-{locale}.png"
            screenshot_file: str = banner.get("screenshot", "02-game-timer.png")
            render_uac_banner_png(
                headline=banner[headline_key],
                subtitle=banner[subtitle_key],
                cta=banner[cta_key],
                brand=brand,
                icon_path=icon_path,
                output_path=output_path,
                screenshots_dir=screenshots_dir,
                output_dir=output_dir,
                locale=locale,
                screenshot_file=screenshot_file,
            )
            rendered.append(output_path)
    return rendered
