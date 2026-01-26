package com.nect.api.domain.user.oauth2.handler;

import com.nect.api.domain.user.oauth2.dto.GoogleOAuth2UserInfo;
import com.nect.api.domain.user.oauth2.dto.KakaoOAuth2UserInfo;
import com.nect.api.domain.user.oauth2.exception.OAuth2ProviderNotFound;
import com.nect.api.domain.user.oauth2.exception.UnsupportedOAuth2Provider;
import com.nect.core.entity.user.User;
import com.nect.core.repository.user.UserRepository;
import com.nect.core.entity.user.enums.UserType;
import com.nect.core.entity.user.enums.Job;
import com.nect.api.global.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2EventHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;

	@Value("${app.oauth2.redirect-uri:http://localhost:3000/auth/callback}")
	private String redirectUri;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    Authentication authentication) throws IOException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		String registrationId = null;

		if (authentication instanceof OAuth2AuthenticationToken) {
			registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
		}

		if (registrationId == null) {
			throw new OAuth2ProviderNotFound();
		}

		User user;
		if ("kakao".equalsIgnoreCase(registrationId)) {
			user = handleKakaoLogin(oAuth2User);
		} else if ("google".equalsIgnoreCase(registrationId)) {
			user = handleGoogleLogin(oAuth2User);
		} else {
			throw new UnsupportedOAuth2Provider(registrationId);
		}

		String accessToken = jwtUtil.generateAccessToken(user.getUserId());
		String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

		String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
				.queryParam("accessToken", accessToken)
				.queryParam("refreshToken", refreshToken)
				.build().toUriString();

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	private User handleKakaoLogin(OAuth2User oAuth2User) {
		KakaoOAuth2UserInfo userInfo = new KakaoOAuth2UserInfo(oAuth2User.getAttributes());

		String socialId = userInfo.getSocialId();
		return userRepository.findBySocialProviderAndSocialId("kakao", socialId)
				.orElseGet(() -> {
					String nickname = userInfo.getNickname() != null ? userInfo.getNickname() : "Kakao User";
					User newUser = User.builder()
							.email(userInfo.getEmail())
							.nickname(nickname)
							.name(nickname)
							.socialProvider("kakao")
							.socialId(socialId)
							.userType(UserType.MEMBER)
							.job(Job.OTHER)
							.build();
					return userRepository.save(newUser);
				});
	}

	private User handleGoogleLogin(OAuth2User oAuth2User) {
		GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());

		String socialId = userInfo.getSocialId();
		return userRepository.findBySocialProviderAndSocialId("google", socialId)
				.orElseGet(() -> {
					String name = userInfo.getName() != null ? userInfo.getName() : "Google User";
					User newUser = User.builder()
							.email(userInfo.getEmail())
							.nickname(name)
							.name(name)
							.socialProvider("google")
							.socialId(socialId)
							.userType(UserType.MEMBER)
							.job(Job.OTHER)
							.build();
					return userRepository.save(newUser);
				});
	}
}