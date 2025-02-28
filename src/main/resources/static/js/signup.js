document.addEventListener('DOMContentLoaded', () => {
    const signupForm = document.getElementById('signupForm');
    const responseMessage = document.getElementById('responseMessage');
    const news2Select = document.getElementById('news2');
    const news3Select = document.getElementById('news3');
    const news4Select = document.getElementById('news4');
    const news5Select = document.getElementById('news5');
    const news6Select = document.getElementById('news6');
    const sendVerificationCodeButton = document.getElementById('sendVerificationCode');
    const verifyCodeButton = document.getElementById('verifyCode');
    const checkUsernameButton = document.getElementById('checkUsername');
    const checkNicknameButton = document.getElementById('checkNickname');
    const emailError = document.getElementById('emailError');
    const verificationCodeError = document.getElementById('verificationCodeError');
    const usernameError = document.getElementById('usernameError');
    const nicknameError = document.getElementById('nicknameError');
    let isEmailVerified = false;
    let isUsernameChecked = false;
    let isNicknameChecked = false;

    // 소식 불러오기
    fetch('/v1/news')
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

    function toggleOptions(isReceiving) {
        const additionalOptions = document.getElementById('additionalOptions');
        if (isReceiving) {
            additionalOptions.style.display = 'block';
        } else {
            additionalOptions.style.display = 'none';
        }
    }
    window.toggleOptions = toggleOptions;

    // 인증코드 보내기
    sendVerificationCodeButton.addEventListener('click', async () => {
        const email = document.getElementById('email').value;

        try {
            const response = await fetch(`/v1/email/verification-request?email=${encodeURIComponent(email)}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({email})
            });

            const result = await response.json();

            if (response.ok) {
                emailError.textContent = result.response;
            } else {
                emailError.textContent = `Error: ${result.message}`;
            }
        } catch (error) {
            emailError.textContent = `Error: ${error.message}`;
        }
    });

    // 인증코드 확인하기
    verifyCodeButton.addEventListener('click', async () => {
        const email = document.getElementById('email').value;
        const code = document.getElementById('verificationCode').value;

        try {
            const response = await fetch(
                `/v1/email/code-verification?email=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({email, code})
                });

            const result = await response.json();

            if (response.ok) {
                isEmailVerified = true;
                verificationCodeError.textContent = '인증이 완료되었습니다';
                verifyCodeButton.disabled = true;
            } else {
                verificationCodeError.textContent = `Error: ${result.message}`;
            }
        } catch (error) {
            verificationCodeError.textContent = `Error: ${error.message}`;
        }
    });

    // 아이디 중복 확인
    checkUsernameButton.addEventListener('click', async () => {
        const username = document.getElementById('username').value;

        try {
            const response = await fetch(`/v1/users/check-username?username=${encodeURIComponent(username)}`, {
                method: 'GET'
            });

            const result = await response.json();

            if (response.ok) {

                if (response.ok && !result.response) {
                    isUsernameChecked = true;
                    usernameError.textContent = '사용 가능한 아이디입니다.';
                } else {
                    isUsernameChecked = false;
                    usernameError.textContent = '사용 불가능한 아이디입니다.';
                }
            } else {
                isUsernameChecked = false;
                usernameError.textContent = `Error: ${result.message}`;
            }
        } catch (error) {
            isUsernameChecked = false;
            usernameError.textContent = `Error: ${error.message}`;
        }
    });

    // 닉네임 중복 확인
    checkNicknameButton.addEventListener('click', async () => {
        const nickname = document.getElementById('nickname').value;

        try {
            const response = await fetch(`/v1/users/check-nickname?nickname=${encodeURIComponent(nickname)}`, {
                method: 'GET'
            });

            const result = await response.json();
            if (response.ok) {

                if (response.ok && !result.response) {
                    isNicknameChecked = true;
                    nicknameError.textContent = '사용 가능한 닉네임입니다.';
                } else {
                    isNicknameChecked = false;
                    nicknameError.textContent = '사용 불가능한 닉네임입니다.';
                }
            } else {
                isNicknameChecked = false;
                nicknameError.textContent = `Error: ${result.message}`;
            }
        } catch (error) {
            isNicknameChecked = false;
            nicknameError.textContent = `Error: ${error.message}`;
        }
    });


    // 회원가입 제출
    signupForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        if (!isEmailVerified) {
            responseMessage.textContent = '이메일 인증을 완료해주세요.';
            return;
        }

        if (!isUsernameChecked) {
            responseMessage.textContent = '아이디 중복 확인을 해주세요.';
            return;
        }

        if (!isNicknameChecked) {
            responseMessage.textContent = '닉네임 중복 확인을 해주세요.';
            return;
        }

        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (password !== confirmPassword) {
            passwordError.textContent = '비밀번호가 일치하지 않습니다.';
            alert('비밀번호가 일치하지 않습니다.');
            return;
        } else {
            passwordError.textContent = '';
        }

        // 최소 하나의 뉴스 선택 확인
        const news2 = document.getElementById('news2').value;
        const news3 = document.getElementById('news3').value;
        const news4 = document.getElementById('news4').value;
        const news5 = document.getElementById('news5').value;
        const news6 = document.getElementById('news6').value;
        if (document.querySelector('input[name="yeongdae"]:checked').value === "not_receive" && !news2 && !news3 && !news4 && !news5) {
            responseMessage.textContent = '적어도 하나의 소식을 선택해주세요.';
            alert('적어도 하나의 소식을 선택해주세요.');
            return;
        }

        const subNewsNamesSet = new Set();
        const keywords = [];

        if (news2) subNewsNamesSet.add(news2);
        if (news3) subNewsNamesSet.add(news3);
        if (news4) subNewsNamesSet.add(news4);
        if (news5) subNewsNamesSet.add(news5);
        if (news6) subNewsNamesSet.add(news6);

        const subNewsNames = Array.from(subNewsNamesSet);


        if (document.querySelector('input[name="yeongdae"]:checked').value === "receive") {
            subNewsNames.push("영대소식");
            document.querySelectorAll('input[name="subNews"]:checked').forEach(checkbox => {
                keywords.push(checkbox.value);
            });
        }

        let subStatus = false;
        let dailySubStatus = false;
        const selectedNewsType = document.querySelector('input[name="newsType"]:checked');

        if (!selectedNewsType) {
            alert('알림 방식을 선택해주세요.');
            return;
        } else if (selectedNewsType.value === 'hourly') {
            subStatus = true;
        } else if (selectedNewsType.value === 'daily') {
            dailySubStatus = true;
        } else if (selectedNewsType.value === 'all') {
            subStatus = true;
            dailySubStatus = true;
        }

        const formData = {
            email: document.getElementById('email').value,
            nickname: document.getElementById('nickname').value,
            username: document.getElementById('username').value,
            password: password,
            subNewsNames: subNewsNames,
            verificationCode: document.getElementById('verificationCode').value,
            keywords: keywords,
            subStatus: subStatus,
            dailySubStatus: dailySubStatus
        };

        try {
            const response = await fetch('/v1/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            const result = await response.json();
            if (response.ok) {
                alert('회원가입이 성공적으로 완료되었습니다.');
                window.location.href = '/';
            } else {
                let errorMessages = '';
                for (const [field, message] of Object.entries(result)) {
                    errorMessages += `${field}: ${message}\n`;
                }
                alert(`Error: ${errorMessages}`);
            }
        } catch (error) {
            responseMessage.textContent = `Error: ${error.message}`;
        }
    });
});
