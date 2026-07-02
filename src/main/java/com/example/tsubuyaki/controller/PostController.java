/*
 * 投稿画面の表示、本文検索、タグ検索、投稿作成、いいね操作を受け付ける
 * Spring MVC コントローラ。
 */
package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.ClientHashService;
import com.example.tsubuyaki.service.PostLikeService;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@Controller
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final ClientHashService clientHashService;

    public PostController(PostService postService, PostLikeService postLikeService,
            ClientHashService clientHashService) {
        this.postService = postService;
        this.postLikeService = postLikeService;
        this.clientHashService = clientHashService;
    }

    /**
     * 投稿一覧を表示する。
     *
     * <p>検索キーワードが未指定または空白だけの場合は最新投稿を表示し、
     * キーワードがある場合は本文検索の結果だけを表示する。検索欄の入力値は
     * 前後空白を取り除いた状態で画面へ戻す。</p>
     *
     * @param q 本文検索キーワード
     * @param model ビューへ渡すモデル
     * @return 投稿一覧テンプレート名
     */
    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        String keyword = normalizeKeyword(q);
        model.addAttribute("posts", keyword.isEmpty() ? postService.latest() : postService.search(keyword));
        model.addAttribute("q", keyword);
        return "posts/list";
    }

    /**
     * 投稿詳細を表示する。
     *
     * <p>存在しない投稿 ID が指定された場合は 404 を返す。詳細画面では投稿本文、
     * アバター色、いいね数を同じ画面で確認できるようにする。</p>
     *
     * @param id 投稿 ID
     * @param model ビューへ渡すモデル
     * @return 投稿詳細テンプレート名
     */
    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        model.addAttribute("postId", id);
        model.addAttribute("likeCount", postLikeService.countLikes(id));
        return "posts/detail";
    }

    /**
     * タグ名に完全一致する投稿一覧を表示する。
     *
     * <p>本文中の {@code #tag} から保存されたタグ名を直接 URL で指定して、
     * そのタグに紐づく投稿だけを一覧テンプレートで表示する。</p>
     *
     * @param name タグ名
     * @param model ビューへ渡すモデル
     * @return 投稿一覧テンプレート名
     */
    @GetMapping("/tags/{name}")
    public String tag(@PathVariable String name, Model model) {
        model.addAttribute("posts", postService.findByTag(name));
        model.addAttribute("q", "");
        model.addAttribute("tag", "");
        return "posts/list";
    }

    /**
     * タグ名の部分一致検索結果を表示する。
     *
     * <p>本文検索とは別のタグ検索欄から使う入口。検索語が空の場合はタグで絞らず
     * 最新投稿を表示し、検索語がある場合はタグ名にその文字列を含む投稿だけを
     * 一覧テンプレートで表示する。</p>
     *
     * @param tag タグ検索キーワード
     * @param model ビューへ渡すモデル
     * @return 投稿一覧テンプレート名
     */
    @GetMapping("/tags")
    public String tagSearch(@RequestParam(name = "tag", required = false) String tag, Model model) {
        String keyword = normalizeKeyword(tag);
        model.addAttribute("posts", keyword.isEmpty() ? postService.latest() : postService.searchByTag(keyword));
        model.addAttribute("q", "");
        model.addAttribute("tag", keyword);
        return "posts/list";
    }

    /**
     * 新規投稿フォームを表示する。
     *
     * <p>投稿者名、本文、任意のアバター色を入力できる空のフォームオブジェクトを
     * ビューへ渡す。</p>
     *
     * @param model ビューへ渡すモデル
     * @return 新規投稿フォームテンプレート名
     */
    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    /**
     * 新規投稿を保存する。
     *
     * <p>投稿者名と本文はバリデーション対象にし、アバター色は未指定でも許可する。
     * 本文中のタグ抽出と保存はサービス層へ委譲する。</p>
     *
     * @param postForm 入力フォーム
     * @param bindingResult バリデーション結果
     * @return エラー時はフォーム、成功時は投稿一覧へのリダイレクト
     */
    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }

        postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getAvatarColor());
        return "redirect:/posts";
    }

    /**
     * 投稿へのいいね状態を切り替える。
     *
     * <p>認証機能がないため、リクエスト元 IP と User-Agent から作った短いハッシュを
     * 利用者識別子として扱う。対象投稿が存在しない場合は 404 を返す。</p>
     *
     * @param id 投稿 ID
     * @param request HTTP リクエスト
     * @return 投稿詳細へのリダイレクト
     */
    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        try {
            postLikeService.toggleLike(id,
                    clientHashService.generate(request.getRemoteAddr(), request.getHeader("User-Agent")));
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "redirect:/posts/" + id;
    }

    /**
     * 検索キーワードを画面表示と検索条件で共通利用できる形に整える。
     *
     * <p>{@code null} は空文字として扱い、前後空白だけで検索結果が変わらないように
     * トリムする。</p>
     *
     * @param q 入力された検索キーワード
     * @return 正規化した検索キーワード
     */
    private static String normalizeKeyword(String q) {
        return q == null ? "" : q.trim();
    }
}
