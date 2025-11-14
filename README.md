# JavaBean 모바일 카페 앱 (메인 화면)

## 레이아웃 구조 (Thymeleaf Fragment 활용)

이 프로젝트는 Thymeleaf의 Fragment 기능을 사용하여 `userBaseLayout.html` 파일을 기반으로 공통 레이아웃(상단바, 하단 탭바 등)을 적용했습니다!

### Fragment 적용 방법

모든 메인 페이지(로그인,회원가입,관리자페이지 제외)는 반드시 `th:replace` 구문을 사용하여 `userBaseLayout.html`의 `setContent` Fragment를 호출해주세요!!

```html
<html lang="ko" xmlns:th="http://www.thymeleaf.org">
<th:block th:replace="~{./include/userBaseLayout :: setContent( ~{:: .wrap} ) }">
    <div class="wrap">
        ...이곳에 메인content 작성
    </div>
</th:block>
</html>
```

메인컨텐츠는 꼭 .wrap 안에 작성 부탁드립니다..!!


# git 사용법

### 모든 작업은 master 브렌치에서 하시는게 아니라 본인의 브렌치에서 모든 작업을 하셔야 합니다!!

처음 모든 작업을 시작하실때 원격저장소에서 sync fork를 누르신 후 git pull origin master로 땡겨오신 다음, 자신의 브렌치를 로컬환경에서 만들어서 작업하시면 됩니다.

## git push 전 해야할 일!

git push를 하시기 전 원격저장소에서 똑같이 sync fork를 누르신 후 
git stash로 작업하시던 결과물을 임시저장소에 저장하신 후, 
git pull origin master로 땡겨오신 다음 git stash apply (git stash pop)으로 
깃을 최신화 시키신 다음 git push origin 본인브렌치명 으로 하시면 됩니다.