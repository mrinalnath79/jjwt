package io.jsonwebtoken.impl

import io.jsonwebtoken.impl.security.Randoms
import io.jsonwebtoken.impl.security.TestKeys
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.lang.Collections
import io.jsonwebtoken.security.EcPrivateJwk
import io.jsonwebtoken.security.EcPublicJwk
import io.jsonwebtoken.security.Jwks
import org.junit.Before
import org.junit.Test

import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger

import static org.junit.Assert.*

/**
 * @since JJWT_RELEASE_VERSION
 */
class DefaultJweHeaderTest {

    private DefaultJweHeader header

    @Before
    void setUp() {
        header = new DefaultJweHeader()
    }

    @Test
    void testAlgorithm() {
        header.setAlgorithm('foo')
        assertEquals 'foo', header.getAlgorithm()

        header = new DefaultJweHeader([alg: 'bar'])
        assertEquals 'bar', header.getAlgorithm()
    }

    @Test
    void testEncryptionAlgorithm() {
        header.put('enc', 'foo')
        assertEquals 'foo', header.getEncryptionAlgorithm()

        header = new DefaultJweHeader([enc: 'bar'])
        assertEquals 'bar', header.getEncryptionAlgorithm()
    }

    @Test
    void testJwkSetUrl() {
        URI uri = new URI('https://github.com/jwtk/jjwt')
        header.setJwkSetUrl(uri)
        assertEquals uri, header.getJwkSetUrl()
        assert uri.toString(), header.get('jku')
    }

    @Test
    void testJwk() {
        EcPrivateJwk jwk = Jwks.builder().forEcKeyPair(TestKeys.ES256.pair).build()
        EcPublicJwk pubJwk = jwk.toPublicJwk()
        header.setJwk(pubJwk)
        assertEquals pubJwk, header.getJwk()
    }

    @Test
    void testX509CertChain() {
        def bundle = TestKeys.RS256
        List<String> encodedCerts = Collections.of(Encoders.BASE64.encode(bundle.cert.getEncoded()))
        header.setX509CertificateChain(bundle.chain)
        assertEquals bundle.chain, header.getX509CertificateChain()
        assertEquals encodedCerts, header.get('x5c')
    }

    @Test
    void testX509CertSha1Thumbprint() {
        byte[] thumbprint = new byte[16] // simulate
        Randoms.secureRandom().nextBytes(thumbprint)
        String encoded = Encoders.BASE64URL.encode(thumbprint)
        header.setX509CertificateSha1Thumbprint(thumbprint)
        assertArrayEquals thumbprint, header.getX509CertificateSha1Thumbprint()
        assertEquals encoded, header.get('x5t')
    }

    @Test
    void testX509CertSha256Thumbprint() {
        byte[] thumbprint = new byte[32] // simulate
        Randoms.secureRandom().nextBytes(thumbprint)
        String encoded = Encoders.BASE64URL.encode(thumbprint)
        header.setX509CertificateSha256Thumbprint(thumbprint)
        assertArrayEquals thumbprint, header.getX509CertificateSha256Thumbprint()
        assertEquals encoded, header.get('x5t#S256')
    }

    @Test
    void testCritical() {
        Set<String> crits = Collections.setOf('foo', 'bar')
        header.setCritical(crits)
        assertEquals crits, header.getCritical()
    }

    @Test
    void testGetName() {
        assertEquals 'JWE header', header.getName()
    }

    @Test
    void testP2cByte() {
        header.put('p2c', Byte.MAX_VALUE)
        assertEquals 127, header.getPbes2Count()
    }

    @Test
    void testP2cShort() {
        header.put('p2c', Short.MAX_VALUE)
        assertEquals 32767, header.getPbes2Count()
    }
    @Test
    void testP2cInt() {
        header.put('p2c', Integer.MAX_VALUE)
        assertEquals 0x7fffffff as Integer, header.getPbes2Count()
    }

    @Test
    void testP2cAtomicInteger() {
        header.put('p2c', new AtomicInteger(Integer.MAX_VALUE))
        assertEquals 0x7fffffff as Integer, header.getPbes2Count()
    }

    @Test
    void testP2cString() {
        header.put('p2c', "100")
        assertEquals 100, header.getPbes2Count()
    }

    @Test
    void testP2cZero() {
        try {
            header.put('p2c', 0)
            fail()
        } catch (IllegalArgumentException expected) {
            String msg = "Invalid JWE header 'p2c' (PBES2 Count) value: 0. " +
                    "Cause: Value is not a positive integer."
            assertEquals msg, expected.getMessage()
        }
    }

    @Test
    void testP2cNegative() {
        try {
            header.put('p2c', -1)
            fail()
        } catch (IllegalArgumentException expected) {
            String msg = "Invalid JWE header 'p2c' (PBES2 Count) value: -1. " +
                    "Cause: Value is not a positive integer."
            assertEquals msg, expected.getMessage()
        }
    }

    @Test
    void testP2cTooLarge() {
        try {
            header.put('p2c', Long.MAX_VALUE)
            fail()
        } catch (IllegalArgumentException expected) {
            String msg = "Invalid JWE header 'p2c' (PBES2 Count) value: 9223372036854775807. " +
                    "Cause: Value cannot be represented as a java.lang.Integer."
            assertEquals msg, expected.getMessage()
        }
    }

    @Test
    void testP2cDecimal() {
        double d = 42.2348423d
        try {
            header.put('p2c', d)
            fail()
        } catch (IllegalArgumentException expected) {
            String msg = "Invalid JWE header 'p2c' (PBES2 Count) value: $d. " +
                    "Cause: Value cannot be represented as a java.lang.Integer."
            assertEquals msg, expected.getMessage()
        }
    }

    @Test
    void pbe2SaltBytesTest() {
        byte[] salt = new byte[32]
        Randoms.secureRandom().nextBytes(salt)
        header.setPbes2Salt(salt)
        assertArrayEquals salt, header.getPbes2Salt()
    }

    @Test
    void pbe2SaltStringTest() {
        byte[] salt = new byte[32]
        Randoms.secureRandom().nextBytes(salt)
        String val = Encoders.BASE64URL.encode(salt)
        header.put('p2s', val)
        //ensure that even though a Base64Url string was set, we get back a byte[]:
        assertArrayEquals salt, header.getPbes2Salt()
    }

    @Test
    void testAgreementPartyUInfo() {
        String val = "Party UInfo"
        byte[] info = val.getBytes(StandardCharsets.UTF_8)
        header.setAgreementPartyUInfo(info)
        assertArrayEquals info, header.getAgreementPartyUInfo()
        assertEquals val, header.getAgreementPartyUInfoString()
    }

    @Test
    void testAgreementPartyUInfoString() {
        String val = "Party UInfo"
        byte[] info = val.getBytes(StandardCharsets.UTF_8)
        header.setAgreementPartyUInfo(val)
        assertArrayEquals info, header.getAgreementPartyUInfo()
        assertEquals val, header.getAgreementPartyUInfoString()
    }

    @Test
    void testEmptyAgreementPartyUInfo() {
        byte[] info = new byte[0]
        header.setAgreementPartyUInfo(info)
        assertNull header.getAgreementPartyUInfo()
        assertNull header.getAgreementPartyUInfoString()
    }

    @Test
    void testEmptyAgreementPartyUInfoString() {
        String s = '  '
        header.setAgreementPartyUInfo(s)
        assertNull header.getAgreementPartyUInfo()
        assertNull header.getAgreementPartyUInfoString()
    }

    @Test
    void testAgreementPartyVInfo() {
        String val = "Party VInfo"
        byte[] info = val.getBytes(StandardCharsets.UTF_8)
        header.setAgreementPartyVInfo(info)
        assertArrayEquals info, header.getAgreementPartyVInfo()
        assertEquals val, header.getAgreementPartyVInfoString()
    }

    @Test
    void testAgreementPartyVInfoString() {
        String val = "Party VInfo"
        byte[] info = val.getBytes(StandardCharsets.UTF_8)
        header.setAgreementPartyVInfo(val)
        assertArrayEquals info, header.getAgreementPartyVInfo()
        assertEquals val, header.getAgreementPartyVInfoString()
    }

    @Test
    void testEmptyAgreementPartyVInfo() {
        byte[] info = new byte[0]
        header.setAgreementPartyVInfo(info)
        assertNull header.getAgreementPartyVInfo()
        assertNull header.getAgreementPartyVInfoString()
    }

    @Test
    void testEmptyAgreementPartyVInfoString() {
        String s = '  '
        header.setAgreementPartyVInfo(s)
        assertNull header.getAgreementPartyVInfo()
        assertNull header.getAgreementPartyVInfoString()
    }
}
