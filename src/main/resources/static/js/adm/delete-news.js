document.addEventListener('DOMContentLoaded', async () => {
    const logoutButton = document.getElementById('logoutButton');
    const mainPageButton = document.getElementById('mainPageButton');
    const newsManagementButton = document.getElementById('newsManagementButton');
    const userManagementButton = document.getElementById('userManagementButton');
    const fetchInfoButton = document.getElementById('fetchInfoButton');
    const newsList = document.getElementById('newsList');
    const accessToken = localStorage.getItem('accessToken');
    let currentPage = 0;
    const pageSize = 10;
    const pagesPerBlock = 10;

    logoutButton.addEventListener('click', async () => {
        try {
            const response = await fetchWithAuth('/logout', {
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

    newsManagementButton.addEventListener('click', () => {
        window.location.href = '/adm/news-management.html'
    })

    userManagementButton.addEventListener('click', () => {
        window.location.href = '/adm/user-management.html';
    });

    fetchInfoButton.addEventListener('click', () => {
        window.location.href = '/user-info.html';
    });

    async function fetchNews(page) {
        const response = await fetchWithAuth(`/v1/admin/news?page=${page}&size=${pageSize}`, {
            method: 'GET',
            headers: {
                'Authorization': accessToken,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const data = await response.json();
            displayNews(data.content);
            displayPagination(data);
        } else {
            alert('소식 목록을 불러오는 데 실패했습니다.');
        }
    }

    function displayNews(newsData) {
        newsList.innerHTML = '';
        newsData.forEach(news => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><a href="/adm/news-details.html?newsId=${news.id}">${news.newsName}</a></td>
                <td>${news.newsURL}</td>
            `;
            newsList.appendChild(row);
        });
    }

    function displayPagination(data) {
        pagination.innerHTML = '';
        const totalBlocks = Math.ceil(data.totalPages / pagesPerBlock);
        const currentBlock = Math.floor(currentPage / pagesPerBlock);

        // 이전 블록 버튼
        if (currentBlock > 0) {
            const prevBlockButton = document.createElement('span');
            prevBlockButton.textContent = '<';
            prevBlockButton.classList.add('page');
            prevBlockButton.addEventListener('click', () => {
                currentPage = (currentBlock - 1) * pagesPerBlock;
                fetchNews(currentPage);
            });
            pagination.appendChild(prevBlockButton);
        }

        // 현재 블록의 페이지 버튼
        const startPage = currentBlock * pagesPerBlock;
        const endPage = Math.min(startPage + pagesPerBlock, data.totalPages);
        for (let i = startPage; i < endPage; i++) {
            const pageSpan = document.createElement('span');
            pageSpan.textContent = i + 1;
            pageSpan.classList.add('page');
            if (i === currentPage) {
                pageSpan.classList.add('active');
            }
            pageSpan.addEventListener('click', () => {
                currentPage = i;
                fetchNews(currentPage);
            });
            pagination.appendChild(pageSpan);
        }

        // 다음 블록 버튼
        if (currentBlock < totalBlocks - 1) {
            const nextBlockButton = document.createElement('span');
            nextBlockButton.textContent = '>';
            nextBlockButton.classList.add('page');
            nextBlockButton.addEventListener('click', () => {
                currentPage = (currentBlock + 1) * pagesPerBlock;
                fetchNews(currentPage);
            });
            pagination.appendChild(nextBlockButton);
        }
    }

    fetchNews(currentPage);
});
