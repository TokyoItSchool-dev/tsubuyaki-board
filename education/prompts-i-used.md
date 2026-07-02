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

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**: M1 git commitをする指示が漏れていたので指示

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:
