package com.nect.core.entity.user;

import com.nect.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_portfolios")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPortfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_portfolio_id")
    private Long userPortfolioId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "link")
    private String link;

    @Column(name = "file_url")
    private String fileUrl;
}