package com.example.shibakamio2026.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.shibakamio2026.entity.User;

/**
 * 資産モジュール（asset-module）にも同名のリポジトリがあります。実リポジトリには
 * 1つだけ存在すればよいので、統合時はメソッドをマージしてください
 * （こちらには existsByEmail 系メソッドが追加されています）。
 */
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);

	/** メールアドレス変更時、自分自身を除いた重複チェックに使用する。 */
	boolean existsByEmailIgnoreCaseAndUserIdNot(String email, Long userId);
}
