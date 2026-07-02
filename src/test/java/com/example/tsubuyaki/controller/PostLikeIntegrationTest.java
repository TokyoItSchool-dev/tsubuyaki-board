package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostLikeIntegrationTest {

    private static final String REMOTE_ADDR = "192.0.2.10";

    private static final String USER_AGENT = "TsubuyakiTest/1.0";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @BeforeEach
    void setUp() {
        // 外部キー制約を守るため、投稿より先にいいねデータを削除する。
        postLikeRepository.deleteAll();
        // 各テストの前提を固定するため、投稿データを空にする。
        postRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // 後続テストへいいねデータが残らないよう、投稿より先に削除する。
        postLikeRepository.deleteAll();
        // 後続テストへ投稿データが残らないよう、テスト終了時にも削除する。
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("いいね登録_未登録clientHashからPOST_保存して詳細画面へリダイレクトする")
    void いいね登録_未登録clientHashからPOST_保存して詳細画面へリダイレクトする() throws Exception {
        // いいね対象の投稿をDBへ保存し、採番されたidでPOSTできるようにする。
        Post savedPost = savePost();
        // ControllerがHttpServletRequestから生成するclientHashの期待値を、テスト側で独立に算出する。
        String expectedClientHash = clientHash(REMOTE_ADDR, USER_AGENT);

        mockMvc.perform(post("/posts/{id}/likes", savedPost.getId())
                        // CSRF保護が有効なPOSTフォームと同じ条件でリクエストする。
                        .with(csrf())
                        // clientHashの入力になる接続元IPアドレスをMockMvcで擬似的に設定する。
                        .with(request -> {
                            request.setRemoteAddr(REMOTE_ADDR);
                            return request;
                        })
                        // clientHashの入力になるUser-AgentヘッダーをMockMvcで擬似的に設定する。
                        .header("User-Agent", USER_AGENT))
                // トグル成功時はHTTP 302 Foundを返すことを検証する。
                .andExpect(status().isFound())
                // トグル成功後は元の投稿詳細画面へリダイレクトすることを検証する。
                .andExpect(redirectedUrl("/posts/" + savedPost.getId()));

        // 未登録clientHashからのPOSTにより、いいねが1件だけ永続化されたことを検証する。
        assertThat(postLikeRepository.countByPostId(savedPost.getId())).isEqualTo(1);
        // 永続化されたいいねが、IPアドレス + User-AgentのSHA-256先頭8文字で識別されることを検証する。
        assertThat(postLikeRepository.existsByPostIdAndClientHash(savedPost.getId(), expectedClientHash)).isTrue();
    }

    @Test
    @DisplayName("いいね解除_登録済みclientHashから再POST_削除して詳細画面へリダイレクトする")
    void いいね解除_登録済みclientHashから再POST_削除して詳細画面へリダイレクトする() throws Exception {
        // いいね対象の投稿をDBへ保存し、既存いいねの外部キーに利用する。
        Post savedPost = savePost();
        // 同一クライアントからの再POSTを表現するため、事前に同じclientHashのいいねを保存する。
        String clientHash = clientHash(REMOTE_ADDR, USER_AGENT);
        postLikeRepository.save(new PostLike(savedPost, clientHash, LocalDateTime.parse("2026-06-01T10:00:00")));

        mockMvc.perform(post("/posts/{id}/likes", savedPost.getId())
                        // CSRF保護が有効なPOSTフォームと同じ条件でリクエストする。
                        .with(csrf())
                        // 既存いいねと同じclientHashになる接続元IPアドレスを設定する。
                        .with(request -> {
                            request.setRemoteAddr(REMOTE_ADDR);
                            return request;
                        })
                        // 既存いいねと同じclientHashになるUser-Agentヘッダーを設定する。
                        .header("User-Agent", USER_AGENT))
                // 解除成功時もHTTP 302 Foundを返すことを検証する。
                .andExpect(status().isFound())
                // 解除成功後も元の投稿詳細画面へリダイレクトすることを検証する。
                .andExpect(redirectedUrl("/posts/" + savedPost.getId()));

        // 登録済みclientHashからの再POSTにより、該当いいねが削除されたことを検証する。
        assertThat(postLikeRepository.countByPostId(savedPost.getId())).isZero();
        // 同一clientHashのいいねがDBに残っていないことを検証する。
        assertThat(postLikeRepository.existsByPostIdAndClientHash(savedPost.getId(), clientHash)).isFalse();
    }

    @Test
    @DisplayName("いいね登録_存在しないidへPOST_404を返しトグル処理を行わない")
    void いいね登録_存在しないidへPOST_404を返しトグル処理を行わない() throws Exception {
        mockMvc.perform(post("/posts/{id}/likes", 999L)
                        // CSRF保護を通過した後の存在確認で404になることを検証する。
                        .with(csrf())
                        // 存在しない投稿idでも、clientHash生成に必要な接続元IPアドレスは通常どおり渡す。
                        .with(request -> {
                            request.setRemoteAddr(REMOTE_ADDR);
                            return request;
                        })
                        // 存在しない投稿idでも、clientHash生成に必要なUser-Agentヘッダーは通常どおり渡す。
                        .header("User-Agent", USER_AGENT))
                // DBに存在しない投稿idの場合はHTTP 404 Not Foundを返すことを検証する。
                .andExpect(status().isNotFound());

        // 存在しない投稿idへのPOSTでは、いいねが1件も作成されないことを検証する。
        assertThat(postLikeRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("いいね登録_異なるIPまたはUAからPOST_別々のいいねとして永続化する")
    void いいね登録_異なるIPまたはUAからPOST_別々のいいねとして永続化する() throws Exception {
        // 複数クライアントから同じ投稿へいいねする前提として、対象投稿をDBへ保存する。
        Post savedPost = savePost();
        // 1人目のクライアントを識別するclientHashの期待値を算出する。
        String firstClientHash = clientHash("192.0.2.10", "TsubuyakiTest/1.0");
        // 2人目はIPアドレスだけを変え、別clientHashとして扱われることを確認する。
        String secondClientHash = clientHash("192.0.2.20", "TsubuyakiTest/1.0");
        // 3人目はUser-Agentだけを変え、別clientHashとして扱われることを確認する。
        String thirdClientHash = clientHash("192.0.2.10", "AnotherBrowser/2.0");

        mockMvc.perform(post("/posts/{id}/likes", savedPost.getId())
                        // CSRF保護が有効なPOSTフォームと同じ条件で1人目のリクエストを送る。
                        .with(csrf())
                        // 1人目の接続元IPアドレスを設定する。
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        })
                        // 1人目のUser-Agentヘッダーを設定する。
                        .header("User-Agent", "TsubuyakiTest/1.0"))
                // 1人目のいいね登録が成功することを検証する。
                .andExpect(status().isFound());

        mockMvc.perform(post("/posts/{id}/likes", savedPost.getId())
                        // CSRF保護が有効なPOSTフォームと同じ条件で2人目のリクエストを送る。
                        .with(csrf())
                        // 2人目として異なる接続元IPアドレスを設定する。
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.20");
                            return request;
                        })
                        // 2人目のUser-Agentヘッダーは1人目と同じ値にする。
                        .header("User-Agent", "TsubuyakiTest/1.0"))
                // IPアドレスが異なる場合は別いいねとして登録できることを検証する。
                .andExpect(status().isFound());

        mockMvc.perform(post("/posts/{id}/likes", savedPost.getId())
                        // CSRF保護が有効なPOSTフォームと同じ条件で3人目のリクエストを送る。
                        .with(csrf())
                        // 3人目の接続元IPアドレスは1人目と同じ値にする。
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        })
                        // 3人目として異なるUser-Agentヘッダーを設定する。
                        .header("User-Agent", "AnotherBrowser/2.0"))
                // User-Agentが異なる場合は別いいねとして登録できることを検証する。
                .andExpect(status().isFound());

        // IPアドレスまたはUser-Agentが異なる3リクエストが、合計3件のいいねとして永続化されることを検証する。
        assertThat(postLikeRepository.countByPostId(savedPost.getId())).isEqualTo(3);
        // 1人目のclientHashに対応するいいねが永続化されていることを検証する。
        assertThat(postLikeRepository.existsByPostIdAndClientHash(savedPost.getId(), firstClientHash)).isTrue();
        // 2人目のclientHashに対応するいいねが永続化されていることを検証する。
        assertThat(postLikeRepository.existsByPostIdAndClientHash(savedPost.getId(), secondClientHash)).isTrue();
        // 3人目のclientHashに対応するいいねが永続化されていることを検証する。
        assertThat(postLikeRepository.existsByPostIdAndClientHash(savedPost.getId(), thirdClientHash)).isTrue();
    }

    private Post savePost() {
        // いいね対象として利用できる投稿をテスト用に1件保存する。
        return postRepository.save(new Post("alice", "S1のいいね対象投稿", LocalDateTime.parse("2026-06-01T09:30:00")));
    }

    private String clientHash(String remoteAddr, String userAgent) {
        try {
            // 本番実装と同じ仕様で、IPアドレス + User-AgentをSHA-256の16進文字列へ変換する。
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((remoteAddr + userAgent).getBytes(StandardCharsets.UTF_8));
            // clientHash仕様に従い、SHA-256ハッシュ文字列の先頭8文字だけを検証値として使う。
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256が利用できないJVMではテスト継続不能なので、明示的に失敗させる。
            throw new IllegalStateException("SHA-256が利用できないためclientHashを生成できません", e);
        }
    }
}
