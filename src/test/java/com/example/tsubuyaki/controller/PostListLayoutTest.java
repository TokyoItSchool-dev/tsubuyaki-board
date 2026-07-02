package com.example.tsubuyaki.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PostListLayoutTest {

    private static final String[] POST_TEMPLATES = {
            "templates/posts/list.html",
            "templates/posts/detail.html",
            "templates/posts/form.html",
    };

    @Test
    @DisplayName("サイト装飾_草原イメージ_CSSだけで背景と文字色を装飾する")
    void サイト装飾_草原イメージ_CSSだけで背景と文字色を装飾する() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains("--color-sky:");
        assertThat(css).contains("--color-grass:");
        assertThat(css).contains("linear-gradient");
        assertThat(css).contains("var(--color-sky)");
        assertThat(css).contains("var(--color-grass)");
        assertThat(css).contains("box-shadow:");
        assertThat(css).contains("text-shadow:");
        assertThat(css).doesNotContain("url(");
    }

    @Test
    @DisplayName("サイト装飾_文字サイズ_既存のfont-size宣言を変更しない")
    void サイト装飾_文字サイズ_既存のfont_size宣言を変更しない() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains("font-size: 1.5rem");
        assertThat(css).contains("font-size: 0.85rem");
        assertThat(css).contains("font-size: 0.9rem");
        assertThat(css.split("font-size:", -1).length - 1).isEqualTo(3);
    }

    @Test
    @DisplayName("サイト装飾_レイアウト_幅と折り返しのCSSを維持する")
    void サイト装飾_レイアウト_幅と折り返しのCSSを維持する() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains("main");
        assertThat(css).contains("max-width: 720px");
        assertThat(css).contains("padding: 0 1rem");
        assertThat(css).contains(".post");
        assertThat(css).contains("overflow-wrap: anywhere");
        assertThat(css).contains("word-break: break-word");
        assertThat(css).contains("flex-wrap: wrap");
    }

    @Test
    @DisplayName("サイト装飾_落ち葉演出_本文レイアウトに影響しない固定レイヤーを持つ")
    void サイト装飾_落ち葉演出_本文レイアウトに影響しない固定レイヤーを持つ() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(".falling-decoration");
        assertThat(css).contains("position: fixed");
        assertThat(css).contains("inset: 0");
        assertThat(css).contains("pointer-events: none");
        assertThat(css).contains("overflow: hidden");
        assertThat(css).contains("main");
        assertThat(css).contains("position: relative");
        assertThat(css).contains("z-index: 1");
    }

    @Test
    @DisplayName("サイト装飾_落ち葉演出_全画面で軽量JavaScriptをdefer読み込みする")
    void サイト装飾_落ち葉演出_全画面で軽量JavaScriptをdefer読み込みする() throws IOException {
        for (String template : POST_TEMPLATES) {
            String html = new ClassPathResource(template)
                    .getContentAsString(StandardCharsets.UTF_8);

            assertThat(html).contains("<script defer th:src=\"@{/js/falling-leaves.js}\"></script>");
        }
    }

    @Test
    @DisplayName("サイト装飾_落ち葉演出_葉と稀な花を軽量に生成して後始末する")
    void サイト装飾_落ち葉演出_葉と稀な花を軽量に生成して後始末する() throws IOException {
        String javascript = new ClassPathResource("static/js/falling-leaves.js")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(javascript).contains("MAX_DECORATIONS = 14");
        assertThat(javascript).contains("SPAWN_INTERVAL_MS = 1800");
        assertThat(javascript).contains("FLOWER_CHANCE = 0.08");
        assertThat(javascript).contains("falling-decoration__leaf");
        assertThat(javascript).contains("falling-decoration__flower");
        assertThat(javascript).contains("prefers-reduced-motion: reduce");
        assertThat(javascript).contains("document.hidden");
        assertThat(javascript).contains("animationend");
        assertThat(javascript).contains("remove()");
    }

    @Test
    @DisplayName("サイト装飾_落ち葉演出_葉は大きさをランダム化し緑系3色から選ぶ")
    void サイト装飾_落ち葉演出_葉は大きさをランダム化し緑系3色から選ぶ() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);
        String javascript = new ClassPathResource("static/js/falling-leaves.js")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(javascript).contains("LEAF_COLORS = [");
        assertThat(javascript).contains("#86c85a");
        assertThat(javascript).contains("#4f9d45");
        assertThat(javascript).contains("#2f7d46");
        assertThat(javascript).contains("--leaf-size");
        assertThat(javascript).contains("randomBetween(1.15, 2.1)");
        assertThat(css).contains("width: calc(var(--leaf-size) * 1.45)");
        assertThat(css).contains("height: var(--leaf-size)");
        assertThat(css).contains("background: linear-gradient(135deg, var(--leaf-color)");
    }

    @Test
    @DisplayName("サイト装飾_落ち葉演出_右上を基準に画面上端から落ち始める")
    void サイト装飾_落ち葉演出_右上を基準に画面上端から落ち始める() throws IOException {
        String javascript = new ClassPathResource("static/js/falling-leaves.js")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(javascript).contains("randomBetween(58, 98)");
        assertThat(javascript).contains("randomBetween(-18, -4)");
        assertThat(javascript).doesNotContain("randomBetween(96, 116)");
        assertThat(javascript).doesNotContain("randomBetween(-14, 12)");
    }

    @Test
    @DisplayName("サイト装飾_落ち葉演出_稀に落ちる花は大きく一定サイズで表示する")
    void サイト装飾_落ち葉演出_稀に落ちる花は大きく一定サイズで表示する() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);
        String javascript = new ClassPathResource("static/js/falling-leaves.js")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains("width: 0.72rem");
        assertThat(css).contains("height: 0.72rem");
        assertThat(css).contains("0.46rem 0 #f7b7c8");
        assertThat(javascript).doesNotContain("--flower-size");
    }

    @Test
    @DisplayName("サイト装飾_素材_画像を使わずCSSとDOMだけで表現する")
    void サイト装飾_素材_画像を使わずCSSとDOMだけで表現する() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);
        String javascript = new ClassPathResource("static/js/falling-leaves.js")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).doesNotContain("url(");
        assertThat(javascript).doesNotContain("new Image");
        assertThat(javascript).doesNotContain("fetch(");
        assertThat(javascript).doesNotContain(".src");
    }

    @Test
    @DisplayName("投稿検索_検索フォーム_狭い画面でも折り返して表示できるCSSを持つ")
    void 投稿検索_検索フォーム_狭い画面でも折り返して表示できるCSSを持つ() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(".toolbar");
        assertThat(css).contains("display: flex");
        assertThat(css).contains("flex-wrap: wrap");
        assertThat(css).contains(".toolbar__query");
        assertThat(css).contains("min-width: 0");
    }

    @Test
    @DisplayName("いいねボタン_詳細画面_レイアウトが崩れない固定寸法と横並びCSSを持つ")
    void いいねボタン_詳細画面_レイアウトが崩れない固定寸法と横並びCSSを持つ() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(".post__good-button");
        assertThat(css).contains("display: inline-flex");
        assertThat(css).contains("align-items: center");
        assertThat(css).contains("min-width: 2.5rem");
        assertThat(css).contains("min-height: 2.25rem");
        assertThat(css).contains("white-space: nowrap");
    }

    @Test
    @DisplayName("投稿詳細アクション_いいねボタン追加後も_狭い画面で折り返して表示できる")
    void 投稿詳細アクション_いいねボタン追加後も_狭い画面で折り返して表示できる() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(".post__actions");
        assertThat(css).contains("display: flex");
        assertThat(css).contains("flex-wrap: wrap");
        assertThat(css).contains(".post__like-form");
        assertThat(css).contains("flex: 0 0 auto");
    }

    @Test
    @DisplayName("いいねボタン_いいね済みのとき_黄色で表示するCSSを持つ")
    void いいねボタン_いいね済みのとき_黄色で表示するCSSを持つ() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(css).contains(".post__good-button--active");
        assertThat(css).contains("background: #facc15");
        assertThat(css).contains("border-color: #eab308");
    }

    @Test
    @DisplayName("リプライフォーム_詳細画面_返信ボタンを右下に配置し1000文字上限を持つ")
    void リプライフォーム_詳細画面_返信ボタンを右下に配置し1000文字上限を持つ() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);
        String html = new ClassPathResource("templates/posts/detail.html")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("class=\"reply-form\"");
        assertThat(html).contains("maxlength=\"1000\"");
        assertThat(css).contains(".reply-form__actions");
        assertThat(css).contains("justify-content: flex-end");
    }

    @Test
    @DisplayName("リプライツリー_詳細画面_狭い画面でも本文を折り返して階層表示できる")
    void リプライツリー_詳細画面_狭い画面でも本文を折り返して階層表示できる() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);
        String html = new ClassPathResource("templates/posts/detail.html")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).contains("class=\"reply-tree\"");
        assertThat(html).contains("↳");
        assertThat(html).contains("--reply-depth");
        assertThat(css).contains(".reply");
        assertThat(css).contains("overflow-wrap: anywhere");
        assertThat(css).contains("word-break: break-word");
        assertThat(css).contains("margin-left: min(");
    }

    @Test
    @DisplayName("リプライ表示_詳細画面_元投稿直下に返信者一覧を出さず返信ツリーで折り返して表示できる")
    void リプライ表示_詳細画面_元投稿直下に返信者一覧を出さず返信ツリーで折り返して表示できる() throws IOException {
        String css = new ClassPathResource("static/css/app.css")
                .getContentAsString(StandardCharsets.UTF_8);
        String html = new ClassPathResource("templates/posts/detail.html")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html).doesNotContain("class=\"post__reply-authors\"");
        assertThat(html).doesNotContain("class=\"post__reply-author\"");
        assertThat(html).contains("th:text=\"|↳${replyItem.reply.author}|\"");
        assertThat(html).doesNotContain("<button type=\"submit\">更新</button>");
        assertThat(css).doesNotContain(".post__reply-authors");
        assertThat(css).doesNotContain(".post__reply-author");
        assertThat(css).contains(".reply");
        assertThat(css).contains("overflow-wrap: anywhere");
    }
}
