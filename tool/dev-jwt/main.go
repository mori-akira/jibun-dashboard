package main

import (
	"crypto/sha256"
	"encoding/hex"
	"flag"
	"fmt"
	"os"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

type Claims struct {
	TokenUse string `json:"token_use"`
	Username string `json:"username"`
	Email    string `json:"email"`
	ClientId string `json:"client_id"`
	jwt.RegisteredClaims
}

func getEnv(key, def string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return def
}

func main() {
	defaultSecret := "piMH6U2uU9_TYC1kx_Mqp4KJDJd0OZK_"
	secret := flag.String("secret", getEnv("DEV_JWT_SECRET", defaultSecret), "HS256 signing secret (or set DEV_JWT_SECRET)")
	issuer := flag.String("issuer", "http://localhost/dev-issuer", "iss (issuer)")
	audience := flag.String("audience", "jibun-dashboard-local", "aud (audience)")
	clientId := flag.String("clientId", "jibun-dashboard-local", "clientId (client id)")
	sub := flag.String("sub", "test-user1", "sub (subject; user id)")
	userName := flag.String("user", "Test User1", "username claim")
	email := flag.String("email", "test-user1@example.com", "email claim")
	ttl := flag.Duration("ttl", time.Hour, "token lifetime (e.g. 1h, 30m)")

	flag.Parse()

	if len(*secret) < 32 {
		// ワーニング
		fmt.Fprintf(os.Stderr, "warning: secret length is short (%d bytes). HS256 secret should be >= 32 bytes for dev.\n", len(*secret))
	}

	now := time.Now().UTC()
	claims := Claims{
		TokenUse: "access",
		ClientId: *clientId,
		Username: *userName,
		Email:    *email,
		RegisteredClaims: jwt.RegisteredClaims{
			Issuer:    *issuer,
			Subject:   *sub,
			Audience:  []string{*audience},
			IssuedAt:  jwt.NewNumericDate(now),
			ExpiresAt: jwt.NewNumericDate(now.Add(*ttl)),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	// Header "typ":"JWT" はデフォルトで付くが、明示したい場合は次行を有効化
	// token.Header["typ"] = "JWT"

	signed, err := token.SignedString([]byte(*secret))
	if err != nil {
		fmt.Fprintf(os.Stderr, "sign error: %v\n", err)
		os.Exit(1)
	}

	fmt.Println(signed)

	h := sha256.Sum256([]byte(*secret))
	fmt.Fprintf(os.Stderr, "info: issued HS256 dev token (iss=%s, sub=%s, client-id=%s, exp=%s, secret_sha256=%s)\n",
		*issuer, *sub, *clientId, claims.ExpiresAt.Time.Format(time.RFC3339), hex.EncodeToString(h[:8]))
}
