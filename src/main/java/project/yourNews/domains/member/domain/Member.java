package project.yourNews.domains.member.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import project.yourNews.common.entity.BaseTimeEntity;
import project.yourNews.domains.subNews.domain.SubNews;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
@Entity
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(length = 20, unique = true)
    private String username;

    @NotNull
    private String password;

    @NotNull
    @Column(length = 10, unique = true)
    private String nickname;

    @NotNull
    @Column(unique = true)
    private String email;

    @NotNull
    private boolean subStatus;

    private boolean dailySubStatus;

    @Enumerated(EnumType.STRING)
    private Role role;

    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private List<SubNews> subNews;

    /* 유저 정보 업데이트 */
    public void updateInfo(String password, String nickname) {
        this.password = password;
        this.nickname = nickname;
    }

    /* 비밀번호 업데이트 */
    public void updatePass(String password) {
        this.password = password;
    }
}
