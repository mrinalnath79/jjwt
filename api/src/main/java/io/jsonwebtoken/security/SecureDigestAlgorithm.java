package io.jsonwebtoken.security;

import io.jsonwebtoken.Identifiable;

import java.security.Key;

/**
 * A {@link DigestAlgorithm} that requires a {@link Key} to compute and verify the authenticity of digests using either
 * <a href="https://en.wikipedia.org/wiki/Digital_signature">digital signature</a> or
 * <a href="https://en.wikipedia.org/wiki/Message_authentication_code">message
 * authentication code</a> algorithms.
 *
 * <p><b>Standard Implementations</b></p>
 *
 * <p>Constant definitions and utility methods for all JWA (RFC 7518) standard
 * <a href="https://www.rfc-editor.org/rfc/rfc7518.html#section-3">Cryptographic Algorithms for Digital Signatures and
 * MACs</a> are available via the {@link JwsAlgorithms} utility class.</p>
 *
 * <p><b>&quot;alg&quot; identifier</b></p>
 *
 * <p>{@code SecureDigestAlgorithm} extends {@link Identifiable}: the value returned from
 * {@link Identifiable#getId() getId()} will be used as the JWS &quot;alg&quot; protected header value.</p>
 *
 * @param <S> the type of {@link Key} used to create digital signatures or message authentication codes
 * @param <V> the type of {@link Key} used to verify digital signatures or message authentication codes
 * @see MacAlgorithm
 * @see SignatureAlgorithm
 * @since JJWT_RELEASE_VERSION
 */
public interface SecureDigestAlgorithm<S extends Key, V extends Key>
        extends DigestAlgorithm<SecureRequest<byte[], S>, VerifySecureDigestRequest<V>> {
}