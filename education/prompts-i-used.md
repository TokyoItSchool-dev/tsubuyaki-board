# 使ったプロンプトと評価

演習中に Codex CLI に投げた主要なプロンプトを記録する。
`EXERCISES.html` に記載の機能実装や、修正提案に関するプロンプトを記述する。

## 書き方

- どのフェーズで使ったか (例: 投稿一覧、いいね機能、リファクタ)
- 実際に投げたプロンプト本文 (省略せずコピペ)
- 結果: 効いた / 部分的に効いた / 効かなかった
- 振り返り: 次に同じ状況が来たらどう変えるか

---

## プロンプト 1(サンプル)

**フェーズ**: サンプルデータ(シーダー)の追加

**プロンプト本文**:

```
src\main\resources\db\migration\V1__init.sql に基づくH2用のシーダーを作成し、実行する。テストデータとして10個程度のシードデータを作成。H2で起動時に自動投入する。
```

**結果**: 効いた

```
• 提案どおり、H2 プロファイル起動時だけ seed が自動適用される構成にしました。

  差分:

  - src/main/resources/application-h2.yml:20: H2 の Flyway location に classpath:db/seed/h2 を追加。
  - src/main/resources/db/seed/h2/R__seed_posts_h2.sql:6: posts に 10 件投入する H2 専用 repeatable migration を追加。posts_seq は次
    ID が 11 になるよう更新。

  - src/test/resources/application-h2.yml:17: テストは空 DB 前提を維持するため seed location を外す設定を追加。
  - src/test/java/com/example/tsubuyaki/db/H2SeedMigrationTest.java:18: seed 適用で 10 件入ることと、次 sequence 値が 11 になることを
    検証。

  検証:

  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Dtest=H2SeedMigrationTest test 成功
  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Ph2 spring-boot:run "-Dspring-boot.run.arguments=--spring.main.web-
    application-type=none" 成功。ログで Migrating schema "PUBLIC" with repeatable migration "seed posts h2" を確認。

  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Ph2 verify 成功。警告: Rule violated for bundle tsubuyaki-board: lines
    covered ratio is 0.48, but expected minimum is 0.60。日本語要約: 現在のカバレッジは 48% で 60% 未満ですが、このプロファイルでは
    fail 設定ではないためビルドは成功しています。
```

**振り返り**:

シーダーは追加できた。
検証方法を指定しなかったためハーネスであらかじめ定義された検証方法を自動的に起動した。
CI検証は成功しているが、実際に画面表示の確認はできていない(未実装のため)。

---

## プロンプト 2

**フェーズ**: M1

**プロンプト本文**:

```
投稿一覧表示機能を作成する。
- 新着順 (created_at DESC) で最大 50 件を返す
- ビュー: posts/list.html
- 0 件なら「まだ投稿はありません」を表示
- model.posts 属性に List を積む
- 更新ボタンを設ける。押すとページをリロードする。
- 投稿者,内容,投稿日の順に表示する。
- 内容は、適度な文字数で折り返す

以下の観点のテストを用意してから本機能の実装を行う。
- 0件の場合に画面に「まだ投稿はありません」を表示できていること
- 51件以上の投稿がある場合に、新着50件しか表示されないこと
- 更新ボタンがあり、押すとposts/ にリクエストすること
- 投稿は「投稿者,内容,投稿日の順に表示できていること
```

**結果**: 部分的に効いた

- 権限不足で途中で止まってしまった。3を選択すると処理から抜けてしまった。再度テスト実施からお願いすると/tmpで作業を再開し始めた。
```
• Running ./mvnw -B -Dtest=PostRepositoryTest,PostServiceTest,PostControllerTest test


  Would you like to run the following command?

  Environment: local

  Reason: Maven が /workspace/target 配下の生成物へ書けず停止しました。テスト実行のためサンドボックス外で再実行してよろしいですか？

  $ ./mvnw -B -Dtest=PostRepositoryTest,PostServiceTest,PostControllerTest test

› 1. Yes, proceed (y)
  2. Yes, and don't ask again for commands that start with `./mvnw` (p)
  3. No, and tell Codex what to do differently (esc)
```
- JUnitのテストメソッド名が日本語になってしまった。
```java
    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("まだ投稿はありません")));
    }
```
- 画面が開かなくなってしまった。例外が発生している。(例外内容は別途プロンプト履歴に記載のためここでは省略)

**振り返り**:

- 権限周りはwindows/wslの絡みなのでcodexと関係ないが、実行前に権限の確認を行う。(今回は権限の確認を行っても諸々の事情で解消できず)
- テストメソッド名は初回は英語と明示する。次回以降は指定しなくても英語になった。

---

## プロンプト 3

**フェーズ**: M1 JUnitのメソッド名を日本語→英語に修正

**プロンプト本文**:

```
 JUnitのテストメソッド名は日本語ではなく英語でお願いします。
```

**結果**: 効いた

**振り返り**:

- 簡単な指示は問題なく動作
- テストが再実行されるので無駄に時間がかかってしまう。初回プロンプトの時に漏れなく指示した方が良さそう。

---

## プロンプト 4

**フェーズ**: M1 画面が開かなくなったのを修正

**プロンプト本文**:

```
画面を表示すると以下の例外が発生していしまいます。
2026-07-01 10:07:06.294 ERROR [http-nio-8080-exec-1] o.h.e.jdbc.spi.SqlExceptionHelper - ORA-18716: {0}はどのタイム・ゾーンでもありません。DATE
https://docs.oracle.com/error-help/db/ora-18716/
2026-07-01 10:07:06.587 ERROR [http-nio-8080-exec-1] o.a.c.c.C.[.[.[.[dispatcherServlet] - Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.orm.jpa.JpaSystemException: Could not extract column [4] from JDBC ResultSet [ORA-18716: {0}はどのタイム・ゾーンでもありません。DATE
https://docs.oracle.com/error-help/db/ora-18716/] [n/a]] with root cause
java.sql.SQLException: ORA-18716: {0}はどのタイム・ゾーンでもありません。DATE
https://docs.oracle.com/error-help/db/ora-18716/
        at oracle.jdbc.driver.TimestampAccessor.getOffsetDateTime(TimestampAccessor.java:158)
        at oracle.jdbc.driver.Redirector$33.redirect(Redirector.java:841)
        at oracle.jdbc.driver.Redirector$33.redirect(Redirector.java:833)
        at oracle.jdbc.driver.Representation.getObject(Representation.java:598)
        at oracle.jdbc.driver.Accessor.getObject(Accessor.java:1044)
        at oracle.jdbc.driver.OracleStatement.getObject(OracleStatement.java:7422)
        at oracle.jdbc.driver.InsensitiveScrollableResultSet.getObject(InsensitiveScrollableResultSet.java:703)
        at com.zaxxer.hikari.pool.HikariProxyResultSet.getObject(HikariProxyResultSet.java)
        at org.hibernate.type.descriptor.jdbc.TimestampUtcAsOffsetDateTimeJdbcType$2.doExtract(TimestampUtcAsOffsetDateTimeJdbcType.java:111)
        at org.hibernate.type.descriptor.jdbc.BasicExtractor.extract(BasicExtractor.java:44)
        at org.hibernate.sql.results.jdbc.internal.JdbcValuesResultSetImpl.getCurrentRowValue(JdbcValuesResultSetImpl.java:387)
        at org.hibernate.sql.results.internal.RowProcessingStateStandardImpl.getJdbcValue(RowProcessingStateStandardImpl.java:152)
        at org.hibernate.sql.results.graph.basic.BasicResultAssembler.extractRawValue(BasicResultAssembler.java:54)
        at org.hibernate.sql.results.graph.basic.BasicResultAssembler.assemble(BasicResultAssembler.java:60)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.extractConcreteTypeStateValues(EntityInitializerImpl.java:1603)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeEntityInstance(EntityInitializerImpl.java:1329)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeInstance(EntityInitializerImpl.java:1308)
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeInstance(EntityInitializerImpl.java:97)
        at org.hibernate.sql.results.internal.StandardRowReader.coordinateInitializers(StandardRowReader.java:244)
        at org.hibernate.sql.results.internal.StandardRowReader.readRow(StandardRowReader.java:141)
        at org.hibernate.sql.results.spi.ListResultsConsumer.read(ListResultsConsumer.java:249)
        at org.hibernate.sql.results.spi.ListResultsConsumer.consume(ListResultsConsumer.java:201)
        at org.hibernate.sql.results.spi.ListResultsConsumer.consume(ListResultsConsumer.java:35)
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.doExecuteQuery(JdbcSelectExecutorStandardImpl.java:224)
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.executeQuery(JdbcSelectExecutorStandardImpl.java:102)
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.executeQuery(JdbcSelectExecutor.java:91)
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.list(JdbcSelectExecutor.java:165)
        at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.lambda$new$1(ConcreteSqmSelectQueryPlan.java:152)
        at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.withCacheableSqmInterpretation(ConcreteSqmSelectQueryPlan.java:442)
        at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.performList(ConcreteSqmSelectQueryPlan.java:362)
        at org.hibernate.query.sqm.internal.QuerySqmImpl.doList(QuerySqmImpl.java:380)
        at org.hibernate.query.spi.AbstractSelectionQuery.list(AbstractSelectionQuery.java:143)
        at org.hibernate.query.Query.getResultList(Query.java:120)
        at org.springframework.data.jpa.repository.query.JpaQueryExecution$CollectionExecution.doExecute(JpaQueryExecution.java:130)
        at org.springframework.data.jpa.repository.query.JpaQueryExecution.execute(JpaQueryExecution.java:93)
        at org.springframework.data.jpa.repository.query.AbstractJpaQuery.doExecute(AbstractJpaQuery.java:152)
        at org.springframework.data.jpa.repository.query.AbstractJpaQuery.execute(AbstractJpaQuery.java:140)
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker.doInvoke(RepositoryMethodInvoker.java:170)
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker.invoke(RepositoryMethodInvoker.java:158)
        at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.doInvoke(QueryExecutorMethodInterceptor.java:170)
        at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.invoke(QueryExecutorMethodInterceptor.java:149)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
        at org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.invoke(DefaultMethodInvokingMethodInterceptor.java:69)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
        at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:380)
        at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
        at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:138)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
        at org.springframework.data.jpa.repository.support.CrudMethodMetadataPostProcessor$CrudMethodMetadataPopulatingMethodInterceptor.invoke(CrudMethodMetadataPostProcessor.java:136)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
        at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:223)
        at jdk.proxy2/jdk.proxy2.$Proxy141.findTop50ByOrderByCreatedAtDesc(Unknown Source)
        at com.example.tsubuyaki.service.PostService.latest(PostService.java:21)
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
        at java.base/java.lang.reflect.Method.invoke(Method.java:580)
        at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:196)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
        at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:380)
        at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119)
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
        at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:727)
        at com.example.tsubuyaki.service.PostService$$SpringCGLIB$$0.latest(<generated>)
        at com.example.tsubuyaki.controller.PostController.list(PostController.java:20)
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
        at java.base/java.lang.reflect.Method.invoke(Method.java:580)
        at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:257)
        at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:190)
        at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118)
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:986)
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:891)
        at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)
        at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1088)
        at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:978)
        at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014)
        at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:903)
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:564)
        at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885)
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:195)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
        at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
        at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
        at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
        at org.springframework.web.filter.ServerHttpObservationFilter.doFilterInternal(ServerHttpObservationFilter.java:114)
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
        at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
        at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167)
        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90)
        at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:483)
        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115)
        at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93)
        at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)
        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344)
        at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397)
        at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)
        at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:905)
        at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1741)
        at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)
        at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1190)
        at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63)
        at java.base/java.lang.Thread.run(Thread.java:1583)
```

**結果**: 効いた

```
  提案 / 根拠
  Post.createdAt は Instant ですが、DB 定義は TIMESTAMP(6) です。Hibernate 6.6 が Oracle から OffsetDateTime として読み出そうとして、ORA-18716: ... DATE が発生していました。既存 DDL に合わせて Instant の JDBC 型を TIMESTAMP に固定しました。

  差分

  - src/main/resources/application.yml:15
      - hibernate.type.preferred_instant_jdbc_type: TIMESTAMP を追加

  - src/test/java/com/example/tsubuyaki/config/JpaInstantMappingConfigTest.java:12
      - 設定が TIMESTAMP に固定されていることを確認する軽量テストを追加
```

- 画面が表示できるようになった。

**振り返り**:

- 画面を開いて実際に表示できるかどうか？まではcodexには確認できないのかもしれない。人間による画面表示確認は必須。
- 例外内容を伝えると問題なく修正できる。

---

## プロンプト 4

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```
Conventional Commits 形式（feat(post): ...）でコミットしてください。
```

**結果**: 効かなかった

- 権限エラーになってしまった。
```
• 今回の修正対象だけをステージします。既存の未コミット変更が複数あるため、application.yml と追加テストだけをコミット対象にします。

────

• config/ 配下の未追跡ファイルを確認して、追加したテスト 1 ファイルだけを含めます。

────

• コミット対象は確認できました。これから 2 ファイルだけをステージし、コミット前の staged diff を確認します。

────

• git add が .git/index.lock を作れず失敗しました。原文は Read-only file system、日本語要約は「このサンドボックスでは .git が読み取り専用で、ステージングできない」です。コミットには .git への書き込みが必要なので、承認付きで Git 操作を実行します。

■ Conversation interrupted - tell the model what to do differently. Something went wrong? Hit `/feedback` to report the issue.
```
- 調べたところ以下を wsl.conf に設定すると良いとあったので設定するも、効果がなかった。
```conf
[automount]
options = "metadata,uid=1000,gid=1000,umask=22,fmask=11"
```

**振り返り**:

- gitコマンドもディレクトリの権限の影響を受ける。
- 講師に相談し、対処としては手動でコミットすることになった。コミットコメントだけcodexに作ってもらうことにした。
```
Conventional Commits 形式（feat(post): ...）のコミットメッセージを作ってください。
```

---

## プロンプト 5

**フェーズ**: M2

**プロンプト本文**:

```
新規投稿フォームを実装する。
- posts/form.html を表示する。
- フォームバインド用に postForm (PostForm) を model に積む
```

**結果**: 部分的に効いた

**振り返り**:

- テストを先に実装する旨を伝え忘れた。しかし、自動でテストが先に実装された。設定値が聞いているのだと思う。
- 作成されたテストコードのメソッドの並び順が処理単位でまとまらなかった。（間に新規テストケースが追加された）ここまで細かい指示が必要なのだと思う。

---

## プロンプト 6

**フェーズ**: M2 JUnitのメソッド順番修正

**プロンプト本文**:

```
テストコードは同じメソッドに対するテストはまとめておいて欲しいです。
今回ケースだと、PostControllerTestの
newForm_whenRequested_returnsFormViewWithPostForm と list_whenPostsExist_rendersAuthorBodyCreatedAtInOrder が前後していれば大丈夫。
```

**結果**: 効いた

**振り返り**:

- 例を提示することで伝わりやすそうに思ったので試してみた。比較対象がないのでわからないが、期待通りの修正になった。

---

## プロンプト 7

**フェーズ**: M3

**プロンプト本文**:

```
投稿登録 POST /posts処理を実装する。
- フォームで投稿ボタンを押下した場合に本文、投稿者をDBに登録する。
- created_atは現在日時を登録時に設定する。
- idは採番済みの最大値+1を設定する。
- 投稿者は1〜30文字。空白のみはNG。validationを行う。
- 本文は1〜280文字。空白のみはNG。validationを行う。
- バリデーション失敗時
  - status code: 200
  - posts/form を再表示し、エラー内容を表示
- バリデーション成功時
  - status code: 302
  - /posts にリダイレクト

以下の観点のテストを用意してから本機能の実装を行う。
- validationエラー時にエラーが表示されること。
- validation成功時、/posts に画面遷移すること。
- 投稿ボタンを押下すると posts/new にリクエストすること。
- DBに「ID,投稿者,本文,投稿日」が登録できること。(制限があるものはmax値で行う)
```

**結果**: 部分的に効いた

- 途中でフリーズしたと思って切断してしまった。（と思ったら、フリーズしていなかった。）
- テストの観点が足らなかった。

**振り返り**:

- 内容によっては30分以上かかるので表示が変わらなくても待つ必要がある。
- codexの表示が変わらないだけで裏で進行・終了していることがあるので、Ubuntuで`podman logs コンテナ名`で状況を確認することが必要。
- 実装するテストを細かく指定する必要がある。（コード記述前にコードテストケースを考えないといけない）

---

## プロンプト 8

**フェーズ**: M3 途中で中断してしまったものを継続

**プロンプト本文**:

```
途中でフリーズしてしまったので止めてしまいました。作業再開して欲しいです。
```

**結果**: 効いた

- 実はフリーズしていなかったらしく、以降のプロンプトでフリーズしていないよ、時間がかかるよ、と言ってくるようになった。
```
› 途中でフリーズしてしまったので止めてしまいました。作業再開して欲しいです。


• 再開します。前回は対象テスト 11 件が緑まで確認済みで、全体 verify の途中で止まっています。まず現在の編集内容を軽く確認してから、全体検証を再実行します。

• Explored
  └ Read TsubuyakiApplication.java, PostService.java, PostRepositoryTest.java, PostController.java, PostControllerTest.java

────

• 差分は残っています。対象テストは前回緑だったので、ここから全体 verify をもう一度流します。Spring コンテキスト起動が重く 2〜3 分程度かかる見込みです。

─────

• verify はテストフェーズに入りました。出力が止まって見える時間がありますが、WebMvcTest / DataJpaTest の起動待ちです。完了までポーリングします。

• Waited for background terminal · ./mvnw -B -Ph2 verify

─────

• Controller テストの Spring 起動ログまで進んでいます。前回と同じ位置なのでフリーズではなく実行中です。引き続き待ちます。
```
- 途中で止めたことが災いしたか、操作で例外発生する。（codexの問題ではないと思うので「効いた」としている）

**振り返り**:

- 時間がかかる場合はどのくらいかかるか目安を教えてもらうようにした方が良いのかもしれない。
- 端末のスペックがよくないのとwindowsのフォルダをマウントしている都合で時間がかかるので根気よく待つ。

---

## プロンプト 9

**フェーズ**: M3 投稿の操作をしたときに例外発生

**プロンプト本文**:

```
投稿の操作をしたら以下の例外になりました。
  2026-07-01 16:10:00.286 ERROR [http-nio-8080-exec-1] o.h.e.jdbc.spi.SqlExceptionHelper - ORA-00001: 一意制約(TSUBUYAKI.POSTS_PK)に反しています

  https://docs.oracle.com/error-help/db/ora-00001/
  2026-07-01 16:10:00.423 ERROR [http-nio-8080-exec-1] o.a.c.c.C.[.[.[.[dispatcherServlet] - Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed:
  org.springframework.dao.DataIntegrityViolationException: could not execute statement [ORA-00001: 一意制約(TSUBUYAKI.POSTS_PK)に反しています

  https://docs.oracle.com/error-help/db/ora-00001/] [insert into posts (author,body,created_at,id) values (?,?,?,?)]; SQL [insert into posts (author,body,created_at,id) values (?,?,?,?)]; constraint [TSUBUYAKI.POSTS_PK]] with root cause
  oracle.jdbc.OracleDatabaseException: ORA-00001: 一意制約(TSUBUYAKI.POSTS_PK)に反しています

          at oracle.jdbc.driver.T4CTTIoer11.processError(T4CTTIoer11.java:717)
          at oracle.jdbc.driver.T4CTTIoer11.processError(T4CTTIoer11.java:609)
          at oracle.jdbc.driver.T4C8Oall.processError(T4C8Oall.java:1347)
          at oracle.jdbc.driver.T4CTTIfun.receive(T4CTTIfun.java:1100)
          at oracle.jdbc.driver.T4CTTIfun.doRPC(T4CTTIfun.java:408)
          at oracle.jdbc.driver.T4C8Oall.doOALL(T4C8Oall.java:499)
          at oracle.jdbc.driver.T4CPreparedStatement.doOall8(T4CPreparedStatement.java:274)
          at oracle.jdbc.driver.T4CPreparedStatement.executeForRows(T4CPreparedStatement.java:1484)
          at oracle.jdbc.driver.OracleStatement.executeSQLStatement(OracleStatement.java:2008)
          at oracle.jdbc.driver.OracleStatement.doExecuteWithTimeout(OracleStatement.java:1621)
          at oracle.jdbc.driver.OraclePreparedStatement.executeInternal(OraclePreparedStatement.java:3955)
          at oracle.jdbc.driver.OraclePreparedStatement.doExecuteLargeUpdate(OraclePreparedStatement.java:4314)
          at oracle.jdbc.driver.OraclePreparedStatement.executeLargeUpdate(OraclePreparedStatement.java:4291)
          at oracle.jdbc.driver.OraclePreparedStatement.executeUpdate(OraclePreparedStatement.java:4273)
          at oracle.jdbc.driver.OraclePreparedStatementWrapper.executeUpdate(OraclePreparedStatementWrapper.java:997)
          at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)
          at com.zaxxer.hikari.pool.HikariProxyPreparedStatement.executeUpdate(HikariProxyPreparedStatement.java)
          at org.hibernate.engine.jdbc.internal.ResultSetReturnImpl.executeUpdate(ResultSetReturnImpl.java:194)
          at org.hibernate.engine.jdbc.mutation.internal.AbstractMutationExecutor.performNonBatchedMutation(AbstractMutationExecutor.java:134)
          at org.hibernate.engine.jdbc.mutation.internal.MutationExecutorSingleNonBatched.performNonBatchedOperations(MutationExecutorSingleNonBatched.java:55)
          at org.hibernate.engine.jdbc.mutation.internal.AbstractMutationExecutor.execute(AbstractMutationExecutor.java:55)
          at org.hibernate.persister.entity.mutation.InsertCoordinatorStandard.doStaticInserts(InsertCoordinatorStandard.java:194)
          at org.hibernate.persister.entity.mutation.InsertCoordinatorStandard.coordinateInsert(InsertCoordinatorStandard.java:132)
          at org.hibernate.persister.entity.mutation.InsertCoordinatorStandard.insert(InsertCoordinatorStandard.java:104)
          at org.hibernate.action.internal.EntityInsertAction.execute(EntityInsertAction.java:110)
          at org.hibernate.engine.spi.ActionQueue.executeActions(ActionQueue.java:644)
          at org.hibernate.engine.spi.ActionQueue.executeActions(ActionQueue.java:511)
          at org.hibernate.event.internal.AbstractFlushingEventListener.performExecutions(AbstractFlushingEventListener.java:414)
          at org.hibernate.event.internal.DefaultFlushEventListener.onFlush(DefaultFlushEventListener.java:41)
          at org.hibernate.event.service.internal.EventListenerGroupImpl.fireEventOnEachListener(EventListenerGroupImpl.java:127)
          at org.hibernate.internal.SessionImpl.doFlush(SessionImpl.java:1429)
          at org.hibernate.internal.SessionImpl.managedFlush(SessionImpl.java:491)
          at org.hibernate.internal.SessionImpl.flushBeforeTransactionCompletion(SessionImpl.java:2354)
          at org.hibernate.internal.SessionImpl.beforeTransactionCompletion(SessionImpl.java:1978)
          at org.hibernate.engine.jdbc.internal.JdbcCoordinatorImpl.beforeTransactionCompletion(JdbcCoordinatorImpl.java:439)
          at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl.beforeCompletionCallback(JdbcResourceLocalTransactionCoordinatorImpl.java:169)
          at org.hibernate.resource.transaction.backend.jdbc.internal.JdbcResourceLocalTransactionCoordinatorImpl$TransactionDriverControlImpl.commit(JdbcResourceLocalTransactionCoordinatorImpl.java:267)
          at org.hibernate.engine.transaction.internal.TransactionImpl.commit(TransactionImpl.java:101)
          at org.springframework.orm.jpa.JpaTransactionManager.doCommit(JpaTransactionManager.java:562)
          at org.springframework.transaction.support.AbstractPlatformTransactionManager.processCommit(AbstractPlatformTransactionManager.java:795)
          at org.springframework.transaction.support.AbstractPlatformTransactionManager.commit(AbstractPlatformTransactionManager.java:758)
          at org.springframework.transaction.interceptor.TransactionAspectSupport.commitTransactionAfterReturning(TransactionAspectSupport.java:698)
          at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:416)
          at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:119)
          at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
          at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:727)
          at com.example.tsubuyaki.service.PostService$$SpringCGLIB$$0.create(<generated>)
          at com.example.tsubuyaki.controller.PostController.create(PostController.java:38)
          at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
          at java.base/java.lang.reflect.Method.invoke(Method.java:580)
          at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:257)
          at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:190)
          at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118)
          at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:986)
          at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:891)
          at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)
          at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1088)
          at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:978)
          at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1014)
          at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:914)
          at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:590)
          at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885)
          at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658)
          at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:195)
          at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
          at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:51)
          at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
          at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
          at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)
          at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
          at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
          at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
          at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)
          at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
          at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
          at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
          at org.springframework.web.filter.ServerHttpObservationFilter.doFilterInternal(ServerHttpObservationFilter.java:114)
          at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
          at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
          at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
          at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)
          at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)
          at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:164)
          at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:140)
          at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:167)
          at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:90)
          at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:483)
          at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115)
          at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:93)
          at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74)
          at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344)
          at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397)
          at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63)
          at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:905)
          at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1741)
          at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52)
          at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1190)
          at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:659)
          at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:63)
          at java.base/java.lang.Thread.run(Thread.java:1583)
```

**結果**: 効いた

- 例外は解消した。

**振り返り**:

- M1の動作確認をするために手動でDBにデータを投入してしまったことが原因。(シーケンスをnullで登録できなかったのでシーケンス設定されていないと思って手動でIDを採番してしまった。Oracleはpostgresとは挙動が違うのかも。)codexでデータ投入を依頼した方がよかった。
- DB設計書を確認すればよかった。(あるのかどうかは不明) codexにIDの採番方法を確認したらよかった。

---

## プロンプト 10

**フェーズ**: M3 テストケース不足

**プロンプト本文**:

```
以下のテストがたらないので追加してください。
- validation test 空欄の場合入力エラーになること
- validation test 最大文字数を超える場合入力エラーになること
- 登録のテストで最大値が登録できることを確認してください。
```

**結果**: 効いた

- 期待するケースが追加された。

**振り返り**:

- 指示内容の日本語が多少おかしくても実施してくれる。

---

## プロンプト 11

**フェーズ**: M4

**プロンプト本文**:

```
投稿詳細 GET /posts/{id} を実装する。
- 一覧画面にて、article押下時に投稿詳細画面に遷移する。
- posts/detail.html を表示
- 投稿詳細画面には投稿内容の詳細を表示する。
- 存在しない id は 404

以下の観点のテストを用意してから本機能の実装を行う。
- 存在するIDの場合、投稿詳細画面が表示できること。
- 存在しないIDの場合、404エラーになること。
- 一覧画面でarticle押下時、投稿詳細画面に遷移すること。
- 投稿詳細画面に対象のIDの投稿内容が表示されること。
```

**結果**: 部分的に効いた

- プロンプトの実行中、JUnitのタイミングでエラーになっていた。
```
[ERROR] Failed to execute goal org.jacoco:jacoco-maven-plugin:0.8.12:report (jacoco-report) on project tsubuyaki-board: An error has occurred in JaCoCo report generation. Error while creating report: Unknown block type 4b. -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
```

**振り返り**:

- codexとしては正常に動作しているが、環境による不具合が起きることがある。
時間短縮のために作業終了前に実行をかけて終了したが、途中で止まってしまうこともあるので随時確認できるならした方が良い。

---

## プロンプト 12

**フェーズ**: M4 Jacocoの不具合解消

**プロンプト本文**:

```
Jacocoがおかしくなっているのか、テストを実行すると以下のエラーになります。 `mvn clean` をして再度テスト実行してみてください。

[ERROR] Failed to execute goal org.jacoco:jacoco-maven-plugin:0.8.12:report (jacoco-report) on project tsubuyaki-board: An error has occurred in JaCoCo report generation. Error while creating report: Unknown block type 4b. -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
```

**結果**: 効いた

- mvn clean を実行してくれた。
- JUnitが通るようになり、画面も表示できるようになった。

**振り返り**:

- コマンドを指定したら実施してくれる。

---

## プロンプト 13

**フェーズ**: M5

**プロンプト本文**:

```
/actuator/health の動作確認を実施します。
- UPが返却されることを確認
- status codeが200であることを確認
```

**結果**: 効いた

- 変更差分はなく、動作確認だけが実施された。

**振り返り**:

- 変更の要否を正しく判断できていた。

---

## プロンプト 14

**フェーズ**: S1

**プロンプト本文**:

* 他の人のプロンプトを参考に改良した。([]タイトルを追加) [grill-meスキルの内容](https://github.com/ryonakae/dotfiles/blob/master/config/.agents/skills/dig/SKILL.md?plain=1)を追加した。
```
POST /posts/{id}/likes で いいねトグルを実装する。
TDDにて実装してください。

[受け入れ条件]
- 同一 clientHash (= ハッシュ化された IP + UA、SHA-256 の先頭 8 文字) が再度押したら解除
- 詳細画面でいいね数と Like ボタン表示

[実装詳細]
-  一覧画面(list.html)にいいね数の表示を追加する。
  - 一覧画面表示時にいいね数の取得を追加する。
- 詳細画面(detail.html)にLike ボタン、いいね数を追加する。
- Likeボタンを押下時、POST /posts/{id}/likesを呼び出す。
  -  clientHashにてすでにいいねをしているかどうかを判定
  - いいねしていない場合はいいね数を増やす(clientHashを登録)
  - いいねしている場合はいいね数を減らす(clientHashの登録を解除)
  - いいねトグル処理が終わったら、status code: 200とし、詳細画面を表示する（画面移動しない）。
-  /posts/{id}/likesのidが存在しない場合はstatus code: 404にする。

[前提]
以下の観点のテストを用意してから本機能の実装を行う。
- 一覧画面にいいね数の表示があること。
- 一覧画面表示データ取得処理でいいね数が正しく取得できること。(0件、複数件)
- POST /posts/{id}/likesでidが存在しない場合にstatus code: 404になること。
- POST /posts/{id}/likesでidが存在する場合にstatus code: 200になること。詳細画面が表示されること。
- 詳細画面(detail.html)にLike ボタン、いいね数の表示があること。
- clientHashがある場合、いいね数が減ること
- clientHashがない場合、いいね数が増えること

[その他]
- プラン・設計・意思決定のあらゆる側面について、共通の理解に達するまでユーザーに徹底的に質問する。甘い回答や曖昧な返答には突っ込む。設計ツリーの各分岐をたどり、判断の依存関係を一つずつ解決する。「幅」より「深さ」を優先する。新たな洞察が得られなくなるまで1つのテーマを掘り下げてから、次へ進む。
- 質問は必ず一度に一つずつ行うこと。質問には、推奨回答も提示する。
各質問の前に、コードベースの関連箇所を調べる。調べればわかることは聞かない。
質問フォーマット:
---
### Q[番号]: [質問文]

[なぜこの質問が重要か]

- **A** — [選択肢]
- **B** — [選択肢]
- ...

**推奨: [A/B/...]** — [理由]
---
- 論点を掘り尽くしたら終了を提案する。続行を求められれば続ける。
終了時のまとめフォーマット:
---
## まとめ

### 決まったこと
- ...
---
```

**結果**: 部分的に効いた

- grill-meによって詳細を詰めることができた。
  - A
    ```
      ### Q1: いいね状態は post_likes の別テーブルで管理してよいですか？

      既存 DB は posts テーブルのみで、Post Entity にもいいね数や clientHash の保持先がありません。受け入れ条件の「clientHash を登録/解除」「同一 clientHash が再度押したら解除」を正しく満たすには、永続化設計を先に決める必要があります。

      - A — post_likes テーブルを追加する
        post_id, client_hash, created_at を持たせ、(post_id, client_hash) に一意制約を張る。いいね数は COUNT(*) で取得する。

      - B — posts テーブルに like_count だけ追加する
        数は持てるが、clientHash ごとの押下済み判定を別途保持できないため、受け入れ条件を満たすには結局別の保存先が必要になる。

      - C — メモリ上の Map で保持する
        実装は軽いが、アプリ再起動で消える。Repository テストや DB 整合性の観点でも弱い。

      推奨: A — clientHash 単位のトグルを DB 制約で守れるため、同時押下や重複登録に対して一番堅いです。
    ```
  - A
    ```
      ### Q2: clientHash の元になる IP は request.getRemoteAddr() を使う方針でよいですか？

      既存コードに X-Forwarded-For や Forwarded Header の処理、Security/CSRF 設定は見当たりませんでした。今回の仕様は「ハッシュ化された IP + UA、SHA-256 の先頭 8 文字」ですが、どの IP を採用するかで同一ユーザー判定が変わります。

      - A — HttpServletRequest#getRemoteAddr() + User-Agent を使う
        ローカル研修環境では単純で再現しやすい。MockMvc テストでも with(request -> request.setRemoteAddr(...)) と User-Agent ヘッダーで制御できる。

      - B — X-Forwarded-For があれば先頭 IP を優先し、なければ getRemoteAddr() を使う
        プロキシ配下の本番想定に近いが、ヘッダー偽装を許すため信頼できるプロキシ設定がない現状では設計が重くなる。

      - C — IP は使わず User-Agent のみで hash する
        仕様の「IP + UA」から外れる。

      推奨: A — このリポの現状に合い、テストも明確です。プロキシ対応が必要になった時点で B に拡張できます。    
    ```
  - A
    ```
      ### Q3: 一覧/詳細で使う表示モデルは PostView のような DTO を新設してよいですか？

      既存は PostService#latest() と findById() が Post Entity をそのまま返し、テンプレートも ${post.author} のように Entity を直接参照しています。一方、今回の要件では Post の内容に加えて likeCount が必要です。Post Entity に画面用の値を混ぜるか、表示用 DTO を作るかを
      決める必要があります。

      - A — web.dto.PostView を新設する
        Post post と long likeCount を持つ表示モデルにする。テンプレートは ${post.post.author} か、getter 委譲で ${post.author} と ${post.likeCount} にする。

      - B — Post Entity に likeCount フィールドを追加する
        表示は楽だが、DB の posts テーブル列ではない集計値を Entity に混ぜることになり、JPA Entity の責務が曖昧になる。

      - C — Controller で Map<Long, Long> を別 attribute として渡す
        Entity はそのままだが、Thymeleaf 側が ${likeCounts[post.id]} のようになり、詳細/一覧で扱いが散らばる。

      推奨: A — Service が「画面に必要な投稿 + いいね数」を組み立て、Controller/テンプレートは単純に表示できます。Entity に集計値を混ぜないので保守しやすいです。
    ```
  - A
    ```
      ### Q4: post_likes の主キーは (post_id, client_hash) の複合主キーでよいですか？

      既存の posts は手動採番 ID + sequence ですが、いいねは「1 投稿につき同一 clientHash は 1 件だけ」が本質です。ここを DB 制約で表すか、別 ID を持たせて unique 制約を追加するかで Entity/Repository の形が変わります。

      - A — (post_id, client_hash) を複合主キーにする
        PostLikeId を @Embeddable で作り、PostLike が @EmbeddedId を持つ。重複禁止が主キーで表現でき、exists/delete/count が自然。

      - B — id surrogate key を追加し、(post_id, client_hash) に unique 制約を張る
        Entity は単純になるが、いいねに独自 ID の意味は薄い。制約は結局 unique で別途必要。

      - C — post_id と client_hash を通常列にして制約なし
        アプリ側だけで重複を防ぐため、同時押下やバグで重複いいねが入り得る。

      推奨: A — 要件そのものを DB の主キーで表現でき、トグル処理の Repository メソッドも明確になります。
    ```
  - A でお願いします。期待値としては詳細画面を表示状態になり、いいね数が増減した結果が表示されていることを期待しています。
    ```
      ### Q5: POST /posts/{id}/likes 成功時はリダイレクトせず、posts/detail を直接再描画する仕様で確定してよいですか？

      既存の投稿作成 POST は redirect:/posts ですが、今回の受け入れ条件には「status code: 200」「詳細画面を表示する」「画面移動しない」とあります。HTTP 的には POST 後リダイレクトも一般的ですが、その場合 status は 302 になり条件と矛盾します。

      - A — 成功時は 200 OK で posts/detail を直接返す
        URL は /posts/{id}/likes のまま、レスポンス本文として詳細画面 HTML を返す。受け入れ条件の status 200 と「画面移動しない」に最も忠実。

      - B — 成功時は redirect:/posts/{id} にする
        ブラウザ表示 URL は詳細ページに戻るが、status は 302 になるため受け入れ条件から外れる。

      - C — 成功時は JSON {likeCount: ...} を返す
        非同期 UI 向けだが、「詳細画面を表示する」と合わない。既存画面も通常フォーム中心。

      推奨: A — 受け入れ条件とテスト観点にそのまま一致します。
    ```
  - 最終まとめ
    ```
      ## まとめ

      ### 決まったこと

      - いいね状態は post_likes 別テーブルで永続化する。
      - clientHash は HttpServletRequest#getRemoteAddr() と User-Agent を連結し、SHA-256 の先頭 8 文字で生成する。
      - 一覧/詳細の表示モデルは PostView を新設して、投稿内容 + likeCount を渡す。
      - post_likes の主キーは (post_id, client_hash) の複合主キーにする。
      - POST /posts/{id}/likes 成功時はリダイレクトせず、200 OK で posts/detail を直接再描画する。
      - POST 成功後の詳細画面には、更新後のいいね数を表示する。
      - Spring Security / CSRF は今回追加せず、既存構成のまま通常 POST フォームで実装する。

      ### これ以上の質問

      現時点で実装判断に必要な論点は十分に解決しました。続行する場合は、この内容で TDD 実装に入れます。
    ```
- 途中で以下のエラーがでて止まってしまった。
```
■ Quota exceeded. Check your plan and billing details.
```
- JUnitでまたJacocoのエラーが出るようになってしまった。(mvn clean 再度実行依頼)
```
[ERROR] Failed to execute goal org.jacoco:jacoco-maven-plugin:0.8.12:report (jacoco-report) on project tsubuyaki-board: An error has occurred in JaCoCo report generation. Error while creating report: Unknown block type 49. -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
```
- `補足: coverage は 0.20 < 0.60 の warning が出ていますが、現在の POM 設定では warning 扱いのためビルド自体は成功しています。`と言われてしまった。
- Likeを押したことがあるかどうかがわかりにくい。ボタンの文言を切り替えられないだろうか？
- likeボタン押下後、リロードするとトグルされてしまう。（URLが変わってしまうため）

**振り返り**:

- （本来はskill定義に配置するものだと思うが）grill-meスキルを活用することで、指示が曖昧な部分を解消しながら勧められた。
- Jacocoのエラーになった場合は `mvn clean` を実施して欲しいむねをあらかじめ記載しておいた方が良いかもしれない。途中で中断になってしまうとjacocoがエラーになりがちだと思われる。
- 途中からカバレッジ率の確認を忘れてしまっていた。人間はプロンプトに記載するのを忘れてしまうのでルールに明示したい。(結果としてカバレッジ率を目視確認したが通して欲しい部分は通っていた。)

---

## プロンプト 15

**フェーズ**: S1 Likeボタン押下後にリロードするといいねがトグルされてしまう

**プロンプト本文**:

```
Likeボタン押下後、ブラウザでリロードするといいねがトグルされてしまいます。
URLが /posts/{id}/likes になっていることが原因と思われます。
/post/{id}にできないでしょうか？（他に良い方法があれば提案して欲しいです）
```

**結果**: 効いた

- redirect:/posts/{id} するように修正された。

**振り返り**:

- 最初の指示で余計なことを言ってしまったため、挙動がおかしくなってしまっていた。(いいねトグル処理が終わったら、status code: 200とし、詳細画面を表示する（画面移動しない）)
あまり理解していない部分については逆に指定しない方が良いかもしれない。

---

## プロンプト 16

**フェーズ**: S1 Likeを押したことがあるかどうかがわかりにくい

**プロンプト本文**:

```
[依頼事項]
Likeを押したことがあるかどうかがわかりにくいので、いいねしたことがあったら次の押下が解除になるようなことがわかる見た目にして欲しい。

[その他]
- プラン・設計・意思決定のあらゆる側面について、共通の理解に達するまでユーザーに徹底的に質問する。甘い回答や曖昧な返答には突っ込む。設計ツリーの各分岐をたどり、判断の依存関係を一つずつ解決する。「幅」より「深さ」を優先する。新たな洞察が得られなくなるまで1つのテーマを掘り下げてから、次へ進む。
- 質問は必ず一度に一つずつ行うこと。質問には、推奨回答も提示する。
各質問の前に、コードベースの関連箇所を調べる。調べればわかることは聞かない。
質問フォーマット:
---
### Q[番号]: [質問文]

[なぜこの質問が重要か]

- **A** — [選択肢]
- **B** — [選択肢]
- ...

**推奨: [A/B/...]** — [理由]
---
- 論点を掘り尽くしたら終了を提案する。続行を求められれば続ける。
終了時のまとめフォーマット:
---
## まとめ

### 決まったこと
- ...
```

**結果**: 効いた

```
  ### Q1: いいね済み状態は、詳細画面の GET 時に clientHash を計算して判定する方針でよいですか？

  現状の PostView は likeCount だけを持っており、「この閲覧者が既にいいね済みか」は保持していません。見た目を変えるには、詳細画面表示時に remoteAddr + User-Agent から clientHash を作り、post_likes に存在するかを見て likedByCurrentClient のような値をテンプレートへ
  渡す必要があります。

  - A — 詳細画面 GET /posts/{id} で clientHash を計算し、PostView に likedByCurrentClient を追加する
    Like 済みならボタン文言や色を変えられる。POST 後のリダイレクト先でも正しい状態を表示できる。

  - B — GET では判定せず、POST 後だけ一時メッセージで「解除できます」と出す
    初回表示や再訪時に状態が分からないため、依頼の「押したことがあるかどうかがわかりにくい」を根本解決しにくい。

  - C — ブラウザの localStorage だけで Like 済み表示を切り替える
    DB の clientHash 状態とズレる可能性があり、別ブラウザ/UA 変更時の整合性も弱い。

  推奨: A — 現在のトグル判定と同じ clientHash を使うため、表示と実際の次アクションが一致します。
```
```
  ### Q2: いいね済みの見た目は、ボタン文言を「Like」から「Liked（解除）」へ変え、強調色を付ける形でよいですか？

  現状の詳細画面は常に Like ボタンだけで、CSS も .post__likes の件数表示のみです。状態を明確にするには、文字だけでなくボタン色/属性も変える方が読み取りやすく、テストもしやすいです。

  - A — 未いいね: Like、いいね済み: Liked（解除） にして、いいね済みボタンを強調色にする
    次に押すと解除されることが文言で分かり、視覚的にも状態が分かる。

  - B — 未いいね: Like、いいね済み: Unlike にする
    次の操作は分かるが、「現在いいね済み」という状態表示としては少し弱い。

  - C — 文言は常に Like のまま、色だけ変える
    色覚差や見落としがあり、依頼の「わかりにくい」を解消しきれない。

  推奨: A — 現在状態と次アクションの両方が伝わり、既存の簡素な UI に小さく足せます。
```
```
  ## まとめ

  ### 決まったこと

  - 詳細画面 GET /posts/{id} で、現在の閲覧者の clientHash を計算して「いいね済みか」を判定する。
  - PostView に likedByCurrentClient 相当の状態を追加する。
  - 未いいね状態はボタン文言を Like にする。
  - いいね済み状態はボタン文言を Liked（解除） にし、強調色で表示する。
  - 次に押すと解除されることが文言で分かる UI にする。

  ### これ以上の質問

  現時点で実装判断に必要な論点は解決しました。続行する場合は、この内容で TDD 実装に入れます。
```
- 相変わらずJacocoが途中でおかしくなる。。

**振り返り**:

- 見た目の問題はあるように思うが、最低限の機能は実装できた。

---

## プロンプト 17

**フェーズ**: S2

**プロンプト本文**:

```
GET /posts?q=xxx で本文 LIKE 検索を実装する。
TDDにて実装してください。

[受け入れ条件]
- 一覧画面を再利用 (検索ボックスを上部に追加)

[実装詳細]
-  一覧画面(list.html)の上部に検索ボックスと検索ボタンをインラインで追加する。
- 検索ボタン押下時、GET /posts?q=xxx を呼び出す。（xxxは検索ボックスの値）
  - 投稿を検索ボックスの値で部分一致(like検索)するものに絞り込む。
  - 検索結果を一覧画面に表示する。
  - 絞り込み以外はPostService.latest()と同様に、50件をcreated_atの降順取得し、検索結果がない場合は投稿がない旨のメッセージを画面に表示する。

[前提]
以下の観点のテストを用意してから本機能の実装を行う。
- 一覧画面に検索ボックス、検索ボタンの表示があること。
- GET /posts?q=xxx で投稿の部分一致検索が行われること。
- 部分一致の結果が正しいこと。
- 検索条件に%が含まれていても正常に検索できること。（%自体を検索する）
- 0件の場合に画面に「まだ投稿はありません」を表示できていること
- 51件以上一致した場合に、新着50件しか表示されないこと
- 投稿は「投稿者,内容,投稿日の順に表示できていること

[その他]
- プラン・設計・意思決定のあらゆる側面について、共通の理解に達するまでユーザーに徹底的に質問する。甘い回答や曖昧な返答には突っ込む。設計ツリーの各分岐をたどり、判断の依存関係を一つずつ解決する。「幅」より「深さ」を優先する。新たな洞察が得られなくなるまで1つのテーマを掘り下げてから、次へ進む。
- 質問は必ず一度に一つずつ行うこと。質問には、推奨回答も提示する。
各質問の前に、コードベースの関連箇所を調べる。調べればわかることは聞かない。
質問フォーマット:
---
### Q[番号]: [質問文]

[なぜこの質問が重要か]

- **A** — [選択肢]
- **B** — [選択肢]
- ...

**推奨: [A/B/...]** — [理由]
---
- 論点を掘り尽くしたら終了を提案する。続行を求められれば続ける。
終了時のまとめフォーマット:
---
## まとめ

### 決まったこと
- ...
---
```
```
• 実装完了しました。

  GET /posts?q=xxx で本文部分一致検索できるようにし、一覧画面上部に検索ボックスと検索ボタンを追加しました。q は trim し、空・空白のみの場合は通常一覧 latest() を表示します。検索後は入力値を検索ボックスに保持します。

  主な変更:

  - src/main/java/com/example/tsubuyaki/controller/PostController.java: q パラメータによる一覧/検索の分岐を追加
  - src/main/java/com/example/tsubuyaki/service/PostService.java: searchByBody() を追加し、検索結果にもいいね数を付与
  - src/main/java/com/example/tsubuyaki/repository/PostRepository.java: findTop50ByBodyContainingOrderByCreatedAtDesc() を追加
  - src/main/resources/templates/posts/list.html: 検索フォームを追加
  - Controller / Service / Repository テストを追加

  検証:

  - ./mvnw -B clean: 成功
  - ./mvnw -B -Ph2 -Djacoco.skip=true -Dtest=PostControllerTest,PostServiceTest,PostRepositoryTest test: 37件成功
  - ./mvnw -B -Ph2 -Djacoco.skip=true test: 49件成功
  - ./mvnw -B -Ph2 -DskipTests -Djacoco.skip=true verify: Checkstyle 0件、SpotBugs 0件、成功

  通常 JaCoCo 付き実行は以前と同じ fork JVM 終了待ちが出たため、mvn clean 後に JaCoCo 無効で検証しています。
```

**結果**: 効いた

```
  ### Q1: q が空、または空白だけの場合は通常一覧として扱ってよいですか？

  GET /posts?q=xxx の検索仕様は明確ですが、検索ボックスを追加すると GET /posts?q= や空白だけの送信が自然に発生します。ここを決めないと Controller の分岐、Service メソッド、画面上の検索語保持の扱いが曖昧になります。

  - A — q が未指定・空文字・空白だけなら、通常の一覧 latest() と同じ結果を表示する
  - B — 空文字も検索条件として扱い、LIKE 検索に渡す
  - C — 空文字・空白だけなら検索結果 0 件として扱う

  推奨: A — 既存の /posts と挙動が揃い、検索ボックスを空にして検索したときも利用者にとって自然です。
```
```
  ### Q2: 検索語に _ や \ が含まれる場合も、ワイルドカードではなく文字そのものとして検索する方針でよいですか？

  受け入れ条件では % を文字そのものとして検索すると明記されています。SQL の LIKE では % だけでなく _ もワイルドカードです。また、エスケープ文字として \ を使う場合は \ 自体の扱いも決める必要があります。

  - A — %、_、\ はすべて文字そのものとして検索する
  - B — % だけ文字そのものとして扱い、_ は LIKE のワイルドカードとして扱う
  - C — % と _ は文字そのものとして扱うが、\ は特別扱いしない

  推奨: A — 「入力した文字で本文部分一致検索する」という利用者の期待に最も近く、LIKE 特有の挙動が画面利用者に漏れません。
```
```
  ### Q3: 検索後、検索ボックスには入力した検索語を残す方針でよいですか？

  検索結果画面で「何で絞り込んでいるか」が見えないと、一覧画面を再利用したときに通常一覧との区別がつきにくくなります。実装上は q を model に入れて、検索ボックスの value に反映します。

  - A — 検索後も検索ボックスに検索語を残す
  - B — 検索後は検索ボックスを空に戻す

  推奨: A — 検索結果の条件が画面上で分かり、再検索もしやすいためです。
```
```
  ### Q4: 英字の大文字小文字は DB の通常 LIKE と同じ扱い、つまり追加の大小文字無視はしない方針でよいですか？

  本文 LIKE 検索で abc が ABC に一致するかは、DB・照合順序・SQL の書き方で変わります。ここで LOWER() などを入れるかを決めないと、Repository テストの期待値が曖昧になります。

  - A — 追加の大小文字無視はしない。DB の通常 LIKE と同じ扱いにする
  - B — 大文字小文字を無視して検索する
  - C — 日本語中心なので英字の大小文字はテスト対象外にする

  推奨: A — 受け入れ条件に大小文字無視はなく、latest() と同様に余計な変換を入れない方が実装範囲を保てます。
```
```
  ### Q5: 検索語の前後の空白は trim して検索する方針でよいですか？

  q=  研修   のような入力を「研修」で検索するか、前後の空白も本文の一部として検索するかで結果が変わります。Q1 の「空白だけは通常一覧」とも整合させる必要があります。

  - A — 前後の空白は trim して検索する
  - B — 前後の空白も検索語としてそのまま扱う

  推奨: A — 検索フォーム入力として自然で、空白だけの場合を通常一覧に戻す扱いとも一貫します。
```
```
  ## まとめ

  ### 決まったこと

  - q が未指定・空文字・空白だけの場合は、通常一覧 latest() と同じ表示にする。
  - LIKE 検索では %、_、\ をワイルドカードやエスケープ制御ではなく、入力文字そのものとして扱う。
  - 検索後は検索ボックスに検索語を残す。
  - 英字の大文字小文字は DB の通常 LIKE と同じ扱いにし、追加の大小文字無視はしない。
  - 検索語の前後空白は trim して検索する。

  これで実装判断は十分です。既存テストの粒度に合わせて、Repository で LIKE と50件制限、Service で likeCount 付き検索結果、Controller/テンプレートで検索フォームと画面表示を押さえます。
```

**振り返り**:

- 見た目は置いておいて、機能としては問題なく実装できた。
