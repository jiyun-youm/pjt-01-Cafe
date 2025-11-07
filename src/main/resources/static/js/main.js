document.addEventListener('DOMContentLoaded', function() {
    // 1. 필요한 HTML 요소들을 찾습니다.
    // (id="notification-trigger"와 id="notification-popup" 요소를 찾음)
    const bellTrigger = document.getElementById('notification-trigger');
    const popup = document.getElementById('notification-popup');

    // 2. bellTrigger와 popup 요소가 둘 다 존재하는지 확인
    if (bellTrigger && popup) {

        // 3. 벨 아이콘(bellTrigger) 클릭 시 이벤트 처리
        bellTrigger.addEventListener('click', function(event) {
            // (중요) 이벤트 전파를 중단시킵니다.
            // 이것이 없으면, 4번의 document 클릭 이벤트가 바로 실행되어 팝업이 뜨자마자 닫힙니다.
            event.stopPropagation();

            // 'show' 클래스를 붙이거나 떼서 팝업을 켜고 끕니다.
            popup.classList.toggle('show');
        });

        // 4. 팝업이 아닌 문서의 다른 곳을 클릭했을 때 팝업 닫기
        document.addEventListener('click', function() {
            if (popup.classList.contains('show')) {
                popup.classList.remove('show');
            }
        });

        // 5. 팝업창 내부를 클릭했을 때는 닫히지 않도록 함
        popup.addEventListener('click', function(event) {
            // 팝업 내부 클릭 시 4번의 이벤트가 실행되지 않도록 막습니다.
            event.stopPropagation();
        });

    } else {
        // 요소를 찾지 못했을 경우 콘솔에 에러 메시지 출력
        console.error('알림 팝업 요소를 찾을 수 없습니다. (ID 확인 필요)');
    }
});