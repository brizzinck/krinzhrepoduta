package ua.duikt.lavrishko.rental.auth;

/** Токен не пройшов перевірку. reason: missing, malformed, bad_signature, wrong_alg, wrong_issuer, expired. */
public class TokenRejected extends Exception {
    private final String reason;
    private final String sub;
    private final String jti;

    public TokenRejected(String reason, String sub, String jti) {
        super(reason);
        this.reason = reason;
        this.sub = sub;
        this.jti = jti;
    }

    public TokenRejected(String reason) {
        this(reason, null, null);
    }

    public String reason() { return reason; }

    /** Значення з непідтвердженого вмісту токена: лише для журналу, довіряти йому не можна. */
    public String sub() { return sub; }

    public String jti() { return jti; }
}
