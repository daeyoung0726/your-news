document.addEventListener('DOMContentLoaded', () => {
    const createPostButton = document.getElementById('createPostButton');
    const fetchInfoButton = document.getElementById('fetchInfoButton');
    const logoutButton = document.getElementById('logoutButton');
    const mainPageButton = document.getElementById('mainPageButton');
    const news2Select = document.getElementById('news2');
    const news3Select = document.getElementById('news3');
    const news4Select = document.getElementById('news4');
    const news5Select = document.getElementById('news5');
    const news6Select = document.getElementById('news6');
    const updateNewsButton = document.getElementById('updateNewsButton');
    const responseMessage = document.createElement('div');
    const accessToken = localStorage.getItem('accessToken');

    responseMessage.id = 'responseMessage';
    document.querySelector('.update-news-container').appendChild(responseMessage);

    createPostButton.addEventListener('click', () => {
        window.location.href = '/create-post.html';
    });

    fetchInfoButton.addEventListener('click', () => {
        window.location.href = '/user-info.html';
    });

    logoutButton.addEventListener('click', async () => {
        try {
            const response = await fetchWithAuth('/v1/auth/logout', {
                method: 'POST',
                headers: {
                    'Authorization': accessToken,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                window.location.href = '/';
            } else {
                alert('로그아웃 실패. 다시 시도해주세요.');
            }
        } catch (error) {
            alert(`Error: ${error.message}`);
        }
    });

    mainPageButton.addEventListener('click', () => {
        window.location.href = '/main.html';
    });

    function toggleOptions(isReceiving) {
        const additionalOptions = document.getElementById('additionalOptions');
        if (isReceiving) {
            additionalOptions.style.display = 'block';
        } else {
            additionalOptions.style.display = 'none';
        }
    }
    window.toggleOptions = toggleOptions;

    // 소식 불러오기
    fetchWithAuth('/v1/news')
        .then(response => response.json())
        .then(data => {
            data.forEach(news => {
                if (news.newsName !== '영대소식') {
                    let option2 = document.createElement('option');
                    option2.text = news.newsName;
                    news2Select.add(option2);

                    let option3 = document.createElement('option');
                    option3.text = news.newsName;
                    news3Select.add(option3);

                    let option4 = document.createElement('option');
                    option4.text = news.newsName;
                    news4Select.add(option4);

                    let option5 = document.createElement('option');
                    option5.text = news.newsName;
                    news5Select.add(option5);

                    let option6 = document.createElement('option');
                    option6.text = news.newsName;
                    news6Select.add(option6);
                }
            });
        })
        .catch(error => console.error('Error fetching news options:', error));

    // 소식 구독 업데이트
    updateNewsButton.addEventListener('click', async () => {

        const news2 = document.getElementById('news2').value;
        const news3 = document.getElementById('news3').value;
        const news4 = document.getElementById('news4').value;
        const news5 = document.getElementById('news5').value;
        const news6 = document.getElementById('news6').value;

        const newsNamesSet = new Set();
        const keywords = [];

        if (news2) newsNamesSet.add(news2);
        if (news3) newsNamesSet.add(news3);
        if (news4) newsNamesSet.add(news4);
        if (news5) newsNamesSet.add(news5);
        if (news6) newsNamesSet.add(news6);

        const newsNames = Array.from(newsNamesSet);

        if (document.querySelector('input[name="yeongdae"]:checked').value === "receive") {
            newsNames.push("영대소식");
            document.querySelectorAll('input[name="subNews"]:checked').forEach(checkbox => {
                keywords.push(checkbox.value);
            });
        }

        const formData = {
            newsNames: newsNames,
            keywords: keywords
        };

        try {
            const response = await fetchWithAuth('/v1/users/update-sub-news', {
                method: 'PUT',
                headers: {
                    'Authorization': accessToken,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });


            const result = await response.json();
            if (response.ok) {
                alert(result.response);
                window.location.href = '/user-info.html';
            } else {
                alert(result.message);
            }
        } catch (error) {
            responseMessage.textContent = `Error: ${error.message}`;
        }
    });
});
