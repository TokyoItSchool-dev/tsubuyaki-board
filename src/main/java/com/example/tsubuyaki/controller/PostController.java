package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostLikeService;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.service.TagService;
import com.example.tsubuyaki.service.TagTextSegment;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final TagService tagService;

    /**
     * 投稿画面のコントローラを生成する。
     *
     * @param postService 投稿サービス
     * @param postLikeService いいねサービス
     * @param tagService タグサービス
     */
    public PostController(PostService postService, PostLikeService postLikeService, TagService tagService) {
        this.postService = postService;
        this.postLikeService = postLikeService;
        this.tagService = tagService;
    }

    /**
     * 投稿一覧またはキーワード検索結果を表示する。
     *
     * @param q 検索キーワード
     * @param model 画面へ渡すモデル
     * @return 投稿一覧テンプレート名
     */
    @GetMapping({ "/", "/posts" })
    public String list(@RequestParam(required = false) String q, Model model) {
        String keyword = normalizeKeyword(q);
        // キーワードが空なら最新一覧、入力があれば本文 LIKE 検索に切り替える。
        List<Post> posts = keyword.isEmpty() ? postService.latest() : postService.search(keyword);
        addPostListAttributes(model, posts);
        model.addAttribute("q", keyword);
        return "posts/list";
    }

    /**
     * タグ名に一致する投稿一覧を表示する。
     *
     * @param name URL パスから受け取ったタグ名
     * @param model 画面へ渡すモデル
     * @return 投稿一覧テンプレート名
     */
    @GetMapping("/tags/{name}")
    public String tagList(@PathVariable String name, Model model) {
        // タグ検索結果の投稿 ID 順を維持したまま投稿を取得する。
        List<Post> posts = postService.findByIdsInOrder(tagService.postIdsByPathName(name));
        addPostListAttributes(model, posts);
        model.addAttribute("q", "");
        return "posts/list";
    }

    /**
     * 投稿詳細画面を表示する。
     *
     * @param id 投稿 ID
     * @param model 画面へ渡すモデル
     * @param request クライアント情報を取得するリクエスト
     * @return 投稿詳細テンプレート名
     */
    @GetMapping("/posts/{id}")
    public String detail(@PathVariable long id, Model model, HttpServletRequest request) {
        try {
            Post post = postService.findById(id);
            model.addAttribute("post", post);
            model.addAttribute("likeCount", postLikeService.countByPostId(id));
            model.addAttribute("bodySegments", bodySegments(post));
            // 投稿者本人と判断できる場合だけ削除ボタンを表示する。
            model.addAttribute("canDelete", post.getClientHash().equals(clientHash(request)));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿が見つかりません。", e);
        }
        return "posts/detail";
    }

    /**
     * 新規投稿フォームを表示する。
     *
     * @param model 画面へ渡すモデル
     * @return 投稿フォームテンプレート名
     */
    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    /**
     * 投稿へのいいねをトグルし、詳細画面へ戻る。
     *
     * @param id 投稿 ID
     * @param request クライアント情報を取得するリクエスト
     * @return 投稿詳細画面へのリダイレクト
     */
    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable long id, HttpServletRequest request) {
        postLikeService.toggle(id, clientHash(request));
        return "redirect:/posts/" + id;
    }

    /**
     * 投稿を論理削除し、一覧画面へ戻る。
     *
     * @param id 投稿 ID
     * @param request クライアント情報を取得するリクエスト
     * @return 投稿一覧画面へのリダイレクト
     */
    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable long id, HttpServletRequest request) {
        // clientHash が一致しない削除要求は権限なしとして扱う。
        if (!postService.delete(id, clientHash(request))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "投稿を削除できません。");
        }
        return "redirect:/posts";
    }

    /**
     * 入力内容から新規投稿を登録する。
     *
     * @param postForm 投稿フォーム
     * @param bindingResult 入力検証結果
     * @param request クライアント情報を取得するリクエスト
     * @return 登録後のリダイレクトまたは入力フォーム
     */
    @PostMapping("/posts")
    public String create(@Valid PostForm postForm, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }
        try {
            // 削除権限判定に使う clientHash も投稿と一緒に保存する。
            postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getColor(), clientHash(request));
        } catch (DataAccessException e) {
            bindingResult.reject("post.create.failed", "投稿の登録に失敗しました。時間をおいて再度お試しください。");
            return "posts/form";
        }
        return "redirect:/posts";
    }

    /**
     * 投稿一覧画面で共通利用するモデル属性を追加する。
     *
     * @param model 画面へ渡すモデル
     * @param posts 表示対象の投稿一覧
     */
    private void addPostListAttributes(Model model, List<Post> posts) {
        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts(posts));
        model.addAttribute("bodySegmentsByPostId", bodySegmentsByPostId(posts));
    }

    /**
     * 投稿 ID ごとのいいね数を組み立てる。
     *
     * @param posts 表示対象の投稿一覧
     * @return 投稿 ID をキーにしたいいね数
     */
    private Map<Long, Long> likeCounts(List<Post> posts) {
        Map<Long, Long> likeCounts = new LinkedHashMap<>();
        for (Post post : posts) {
            // 未永続化の投稿は画面上で ID 参照できないため集計対象外にする。
            if (post.getId() != null) {
                likeCounts.put(post.getId(), postLikeService.countByPostId(post.getId()));
            }
        }
        return likeCounts;
    }

    /**
     * 投稿 ID ごとの本文表示セグメントを組み立てる。
     *
     * @param posts 表示対象の投稿一覧
     * @return 投稿 ID をキーにした本文セグメント
     */
    private Map<Long, List<TagTextSegment>> bodySegmentsByPostId(List<Post> posts) {
        Map<Long, List<TagTextSegment>> segmentsByPostId = new LinkedHashMap<>();
        for (Post post : posts) {
            // テンプレート側で ID をキーに参照するため、ID がある投稿だけ保持する。
            if (post.getId() != null) {
                segmentsByPostId.put(post.getId(), bodySegments(post));
            }
        }
        return segmentsByPostId;
    }

    /**
     * 投稿本文を通常文字列とタグ文字列のセグメントへ分割する。
     *
     * @param post 投稿
     * @return 本文セグメント一覧
     */
    private List<TagTextSegment> bodySegments(Post post) {
        List<TagTextSegment> segments = tagService.bodySegments(post.getBody());
        // タグ分割結果が空の場合も、本文全体を通常文字列として表示できるようにする。
        if (segments == null || segments.isEmpty()) {
            return List.of(new TagTextSegment(post.getBody(), false));
        }
        return segments;
    }

    /**
     * 検索キーワードを画面表示と検索に使いやすい形へ正規化する。
     *
     * @param q 入力された検索キーワード
     * @return null を空文字にし、前後空白を除去したキーワード
     */
    private String normalizeKeyword(String q) {
        if (q == null) {
            return "";
        }
        return q.strip();
    }

    /**
     * IP アドレスと User-Agent からクライアント識別用ハッシュを作る。
     *
     * @param request クライアント情報を取得するリクエスト
     * @return SHA-256 の先頭 8 文字
     */
    private String clientHash(HttpServletRequest request) {
        String source = request.getRemoteAddr() + "|" + request.getHeader("User-Agent");
        try {
            // 生の IP / UA を保存しないため、SHA-256 の短いハッシュだけを利用する。
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", e);
        }
    }
}
