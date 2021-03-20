package no.difi.move.deploymanager.service.codesigner;

import no.difi.move.deploymanager.action.DeployActionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class GpgServiceImplTest {

    @InjectMocks
    private GpgServiceImpl target;

    private String signedDataFilePath;
    private static final String downloadedSignature = "-----BEGIN PGP SIGNATURE-----\n" +
            "\n" +
            "iQJKBAABCgA0FiEECrr9T6qAnW7siv2YowtoSjCNj8gFAmBUkIQWHGphLmZsYXRl\n" +
            "bjkxQGdtYWlsLmNvbQAKCRCjC2hKMI2PyOcGD/9LhdiWjQTzKnXbOloktHn1vBE5\n" +
            "2lkL14rbZntMHsE1pbah8FBxsQLIpaNj2VfBah4RKRsKD/qNeUnhx72uLd4JMvnm\n" +
            "HS/fZUDmSyU4wsFZPEAt0Vy80fyK9tIqBc+8cgNlfyswSefjXoSKl2IoQQPLeoiG\n" +
            "eJpWJOxg2ZbT9IEIGDRsDhe6oJZ9lM7NL2AdFYWrt56MbjKTA9OW7/icm1t/+uVr\n" +
            "xuuVc7L1iq+5x5b3mFnWJwoBTGR0DB/TijsjVuxSxMg6XVkXdq/GMjA6VmOR62tT\n" +
            "Y5XlSWyBCUVEAWbyeLyMQWdWwD2VEEQiAXODKadXMqZm2JNSLXRupb2tqNR88ZuX\n" +
            "0F090A11Nh6iOBYo9PjMlp46XlSaUoprgxT/JzXB4bfZdt9uGBoU1ZksboMdRINy\n" +
            "zoGyLvcFiQuPkjRb7uh0hicZQV5JiHHduPfSNlaGE+Mg9WaAx0YfUloUdgTvf31R\n" +
            "y0GfFme8DBu9jIENrAcTu0PT6Bi1soe9q7iIe8ohMohrD77RIWXHp5QQL4PXKsfj\n" +
            "1bzXF0jqPhhL9l4a5cCasEGL5t1Abw+MtoX/MGZGluHHc40SCcrJqz6RGrOp13Dw\n" +
            "6Aec3npWpnDtNo6VKSHNBQrfpBgTwdqy/iPDDxKSSIYzdz6//AbFthtTv1G4Q40P\n" +
            "I2j5U6OJbpHEZM6flA==\n" +
            "=tEAH\n" +
            "-----END PGP SIGNATURE-----";
    private static final String anotherSignature = "-----BEGIN PGP SIGNATURE-----\n" +
            "\n" +
            "iQJKBAABCgA0FiEECrr9T6qAnW7siv2YowtoSjCNj8gFAmBUkIQWHGphLmZsYXRl\n" +
            "0F090A11Nh6iOBYo9PjMlp46XlSaUoprgxT/JzXB4bfZdt9uGBoU1ZksboMdRINy\n" +
            "zoGyLvcFiQuPkjRb7uh0hicZQV5JiHHduPfSNlaGE+Mg9WaAx0YfUloUdgTvf31R\n" +
            "y0GfFme8DBu9jIENrAcTu0PT6Bi1soe9q7iIe8ohMohrD77RIWXHp5QQL4PXKsfj\n" +
            "1bzXF0jqPhhL9l4a5cCasEGL5t1Abw+MtoX/MGZGluHHc40SCcrJqz6RGrOp13Dw\n" +
            "6Aec3npWpnDtNo6VKSHNBQrfpBgTwdqy/iPDDxKSSIYzdz6//AbFthtTv1G4Q40P\n" +
            "I2j5U6OJbpHEZM6flA==\n" +
            "=tEAH\n" +
            "-----END PGP SIGNATURE-----";
    private static final String downloadedPublicKey = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "\n" +
            "mQINBGA2CzEBEACnUeeR5qVzXDMqfUxRRwqZDtOwKzl9LNpyf42qo/0Gk1IorIlR\n" +
            "UsV9Da6uRatGfREbim43vcINLZDU2FsWo462B9wNcle/14axIfYRLY7QsyKyQ587\n" +
            "I0ri4voynmfakhnUHmN9EAK9IBornsKozyTXmkaQOZBgWblT5SAp/nmpVev/+MOx\n" +
            "3kRQ5td9sd9SiHY/RtRnjm6YhSZ/XFhqFH3FXA9DjVr+V9ThgK4HWsrbAV5sGsIT\n" +
            "taMBXtXbM8nZ1PvtuwClgJ/ys3HNYHU9vhPI0T3KCbytD6C3V3aJDptBNe5xiOpb\n" +
            "UM63CR0JdnovL3EASr1/QNvhCrmZ5Cq2hDMUpz+fd4AheLtHmET3BuOPtTDiZ1+y\n" +
            "f1Jvq+F8r3csFkL4thkO8uCAbN4vocrTQbCTnS6a2/uvlny0pRSKRb97LxLMVMke\n" +
            "+rt88HVzZzUYLvTyiQIHfJZCAWPnzruemtIyvxworN5DpWwGTk6oDcdJyylQaO/2\n" +
            "XKsi2X7GnhawlMeo9kfmY90e1EYwVJR5V9tnbNVKLAy/owpClOp/OTH4xN+AKGxI\n" +
            "k33qXREfBkLhAyvt7LoyBRfC2IXAjBVAdvsIhREmuDZYLhojX6W0wREchaLotpOT\n" +
            "0H+If2jmAMhSbExnx7Q4OiSkWoEj3exLMhkZldv/4r80DBc4yTj/vNg6GQARAQAB\n" +
            "tC9Kb3JuLWFyZSBGbGF0ZW4gKHRlc3QyKSA8amEuZmxhdGVuOTFAZ21haWwuY29t\n" +
            "PokCVAQTAQoAPhYhBAq6/U+qgJ1u7Ir9mKMLaEowjY/IBQJgNgsxAhsDBQkJZgGA\n" +
            "BQsJCAcCBhUKCQgLAgQWAgMBAh4BAheAAAoJEKMLaEowjY/I6I8QAIe7vfoSMwnV\n" +
            "9dhInj2qOzpdroxz9uNlbxEvROsDJ/nO2i3uzWc5NP+SyJlgIubO9kSQxD0jw04h\n" +
            "di99WPYDu+5Wwx7YdhjRL8nGUdwWM4J/YRkNowBak2fp7W7gSiseuTQMRTW1US7e\n" +
            "PdDHt+7TNMYtNUIB/pN2YMGSdbXq/kHkjIcFEeUpo7snUDNkgStJ8uaWfe37hjh+\n" +
            "QRy2X+vPPqXSznRrVtXi6H11UNaCZ+2l5YlCj9+ecumt6LFihxTcg6COaqG5X9qT\n" +
            "X9WEL7bdcDSw24EO3sHONTAlEqSQdIErhHtfWfz86ZDfbvQ0lQvJzlTjFYHgl3+d\n" +
            "MPmuJKanuElik0BHV2FrzhvssMi2cF4K3djSV4GVIGceCzU1u2UjxnP/JuNE6QOc\n" +
            "EJYjhXhis+ZLPTVT58TRR+/THVMwXgTpdBkl+gsdmvS1LzX4Xbabw38pPv+UXDga\n" +
            "VmzDN3GTkMu9wIHyx9Oon04kQ8+jm9WKZxjw4Bghe6YAFS84dTsnjVEkmSoyS5vE\n" +
            "j0aoDDqhKvd08b3RZWHkYBb8xFrTj4mp3Zt+XYZitECFPy+OR9ko6G5KlYeVc2X/\n" +
            "aCwMUh5mEIcEbSoaNNLFE4bKyZvo29msVq12y9RELSQLEDv5N4mSRZRMdSvCjglQ\n" +
            "Xe5c6soaAIP0lfDsf5Hd2VhkTv3kGsGFuQINBGA2CzEBEACwciEKsrfMeELrSaym\n" +
            "DqTRkqcyPcuoli+x4yi5GgWt71d4Cyq+3NqtmLWJFm6tBYqQHUQ3MdpHtXy8+l04\n" +
            "EOIj265ugOAwBsQ10kj6IvZhBHPDXw2AM1hx/wG3jGgDxbKz88xszVOHX4Zs0R0A\n" +
            "7hTAG1nesZUD1gS0PHYdkZeUTdgvANMHv8yxUP8IP40i+v47sEep91J7LW2ikSCL\n" +
            "9bEaobH1O412zHKv1F9oA5cWxTJ3QgP6KEipiiPTQCIqmA2dPGExOp0d3nYn/FOY\n" +
            "2KI5eHWEaIKxOV1MNWj/JiwFQZNvUgJtTy2+nhB3Cl1jpyQIwhatkcKfg3TGaRVM\n" +
            "mgB2lfSOphWp/o0wGt08QOVcauNhqRn85HM/kWurlsB3akJ3dNhbcKjaOUFJn4qs\n" +
            "s3gG9/gCF6h4Ic2erH88ExeSullTsn0WeEpW+4fzsiDhwFHFnVN6/aB6QEiyh2Xr\n" +
            "K0B+MTjfcZw9Xm42/w+Sn/oXSIHm6IVekQEhr9JrCZaENiy+WFSOHzufiXzpnkkX\n" +
            "SwC6N41EAqQOIb9xpiISWBiyTBosaq/mMkAkprakL7HYoU1x/RGINF38d2PEwDbI\n" +
            "rGaa1N55OVdMZiHvV3vJkLKT9qYLfEI3nRZ8HVs6qLH/hXB42D2FFV6Cx59v6BJh\n" +
            "3shYMOErify1hUKjZCJa/6gUtQARAQABiQI8BBgBCgAmFiEECrr9T6qAnW7siv2Y\n" +
            "owtoSjCNj8gFAmA2CzECGwwFCQlmAYAACgkQowtoSjCNj8iFng/9GCBWLAWl1+MN\n" +
            "k6DKdj+m25L1TJh2sSd9Qck3FjcqGG+IadoOShzFHFKGEpZWMMSbZP0AVHPFdYT1\n" +
            "x2c2H+8wIZAZGD+Xka7sNSqCcppgDcOVlDli0r9Xr9WW/CGGOshiduTxghn1qQSt\n" +
            "a+mmW763JqKtY2LysrHX2MZE7k1WDWgyrcsjPfinL1dIzHS/5vzKaJYmkGl/xeD5\n" +
            "EWNYLGAeYV+rLROhQaPTlU5QFPVdEm1xNOw9fPsBoou/DJjL/CWPOX5SqW6nmuTq\n" +
            "26MpRWzIHbUShMQa7bxDoxDyH1gqM1dUTv8gX8BcrYUQ+Yv+dae3kqK3pHfFFCb1\n" +
            "6IQMUMd3gi/qNwY1D69QXbEwrnRiCMTptvUPwpns7xemS44OqaC0fzYyyrb09/ce\n" +
            "QWDdKp54piVuKab7dSy5X0GETXYN4Y8pZ37NS6lXULaS7DmCStqlxX24U0uT7A7h\n" +
            "wmi3GszEr3jREf5O81xk/H/fdcwpq9RBlx6h8c7EWtW5eXokpL8xHSSQXbDe625r\n" +
            "FZH+Tt5TAL8MyDVyyvNzk+NJmkny0Rxgessskb8cYNw7AYE4vJmo9ivVIpcr6QZJ\n" +
            "H8FM3Vq+n9zQXXcHFzbmGReUrHlSnG4xxdVILyYPSmQklqQLP/xZpC1d5RJ0aqIP\n" +
            "B+y8SZKZLy6BAekFGXCPDjr10igYpA4=\n" +
            "=O5Ts\n" +
            "-----END PGP PUBLIC KEY BLOCK-----\n";

    private static final String anotherPublicKey = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "\n" +
            "mQGNBGArkXwBDADdMiexS3VsuPgdvVWdSdudWYu2qZBjS7b4EnhEThTNEXd8fpxn\n" +
            "EAp6hi11308jIcVaGVhJSkOWESGcxaBuhuXLkQbHT3EvWFLZOv3u6H7ZxWPF9pva\n" +
            "gCEo1MxbmZRM3iq5VXPqltP9zrWOQ6Ofz6pDsMSPqzeIJrSq9LHmcPOrPu47gyUc\n" +
            "XsiNLF5gVaCrSCW8qHk/pQXiwabgF6MXhOrYs5/AtWMjXKAZFX47CWKDfACDzJXE\n" +
            "uZ1bHYPBAs9UU0JS1Lvfprc1DZNbEI7nICu3yCZQ2geX7UGqz9vld8tDUmv8gwN0\n" +
            "CehTyRTbZg9ymYbbGUPCSP/9m6M5oj8R7EKf8/5Lxv7UR3DN1INSv0N17UK/vS8u\n" +
            "ab7u5KLar+favHrkm7pzWaeiU6zJhBVyeT7lUlnyq2zkKiUE2TkJTLZyCXCq6vta\n" +
            "VzqmmOiznuo5LirMFp+fqDSit9jHwj+6VsrXiIjGfAMIhGv+s1HG6G73hVguhs6F\n" +
            "2dFHTZEbKbUF5ckAEQEAAbQIZWZtLXRlc3SJAdQEEwEKAD4WIQRtURq6O8bULC7H\n" +
            "UwT0eSgaNjs3agUCYCuRfAIbAwUJA8JnAAULCQgHAgYVCgkICwIEFgIDAQIeAQIX\n" +
            "gAAKCRD0eSgaNjs3aqOQDAC7YDolJgjSQ1nsgTdEitKYeuMKygrL+HYuF1UJvmKd\n" +
            "oWQ5hnSmMPHPZOvha12XRYQbcr8aLUAO3nrs80Az2UZIt5vjqfrPPRsZ8RK+8ixm\n" +
            "bCO2BRqrw3TLg5s1GFdkHVq8tdK5bskcY0Ji4CE8m+vKu4PB7APl9951d+k95xDP\n" +
            "RU8xL+5HC47XBZB0vYf5dDHFS0J8DXzohnADpaVr6iyLbP/U/KBkmM12kNvBnrg1\n" +
            "GuXY7e3o7Je5fy/VtwsgWJahwXp4AZXBNm1fVALVkrad5E1vAR+xnhDSYOilcJNR\n" +
            "DBh/L+e0tiWP76HmK+Zir0pQuueUf7T+I9OFeeWr6FQ+JzKa4MT2Mxt+ZgYtpq1s\n" +
            "GQPPKvZ7FiGaL/p1P7LlnSA/nBZF7B0LwKdeEaYDxD/yQ3ln0/zChsskcdBK4HAS\n" +
            "AyP/OlNLUmiqTCForOpFpl5HpbMUqsfcLzACUN94hmWjBvStjHY9/lzKDetO2/gB\n" +
            "bfQVt91UFHaPfwzOU4shjbW5AY0EYCuRfAEMAKvHHf+qkRx2cGD7jvRq4K6xPCYi\n" +
            "7kPd7+kePcNnelkZTLwyS4HJm5R2s76Kzwcw35KlXwuBNfhNFxKSVqqCwzoil1p2\n" +
            "NrKQ6VpEpzgwZnDGY1xnjOaMJyfiCWNNC76WYrFnaRYXpuhvnZ12mC5TjkQmO752\n" +
            "A9c+FhVYGLENXRKt1akWtdiM/lK+A900ss/RG6QK3AAKzoLXnkpfpkwQuDxxDbtg\n" +
            "xkNC/+Cplroit0NMdgrKls8t51ewa6xldRieUTkwMGU8dhBUJ5i2z0cuRXoxsP9Q\n" +
            "Y+JLcriM27UTdgKzS8RehZ+FWNrKsHwWsYO+ai9gPjWPpUXL7ejc/MGkm3sfObZM\n" +
            "CQ8IRF08fiWkvUNAFqTKwZ4bCKvBFHLCbu08rn20MEjDk8AzPN3gYuipyIFaRv4h\n" +
            "Vg//uo0lN5b9DyhBXgkKuQLtkqVt8nwnR/GOtil0R1aSvvGzFrx61yhxQ+YKxPUM\n" +
            "Urv2Okz+3+Ayr5zLq1D5Fc0kgUCSllCnkH0upQARAQABiQG8BBgBCgAmFiEEbVEa\n" +
            "ujvG1Cwux1ME9HkoGjY7N2oFAmArkXwCGwwFCQPCZwAACgkQ9HkoGjY7N2pPwAv/\n" +
            "R0fVeoZoNyMGhRMSoNauVvfyc5iYngsMAjT6ZXBJoURpLCic499sWF9xWYSOmPQu\n" +
            "n4vou4URiIbEXcCX4KNNla2W6us0ICrpC4uEd5hXuTyVdRH11GYXElw8IcZz4mYG\n" +
            "Z5BC/1xsaqcPk0PGcQAsNsTZsJBhkfJgJKXcvlCdDplsDHoYaENJzZhjeShMnDCJ\n" +
            "3SZ6CxmbH3kU4gTy9ffkQnTQZCA/tlT4rE2yrussNnqa7ekU2Dlxo3yr9oGs6lRp\n" +
            "lpw/zIP0m6aRVbswgBrSLtrMcJzp0gpdvr8IzH5tIpF9hwDqI0y9FSvJg3a4FZwk\n" +
            "S+IO9469zHnrsmSPqdLlYTujQyjYKm3S5MioQ0NT/lLOI7nxYl4ErYUq560aDr9C\n" +
            "Sl0bxia7u6cF95ho2DEdxqrE8lCf8NxkOmBYCJx7trN9o/HPZQ00mJurdRaxjeXC\n" +
            "ndPraJ0kAn8TA3i33kaDZdffkWRiE1V06KyhLZqmM4BMHggrvHEt5UGWKbVYMC9G\n" +
            "=P1wa\n" +
            "-----END PGP PUBLIC KEY BLOCK-----\n";

    @Before
    public void setUp() throws IOException {
        ClassPathResource resource = new ClassPathResource("/gpg/gpgTest.txt");
        signedDataFilePath = resource.getFile().getAbsolutePath();
    }

    @Test
    public void verify_Success_ShouldVerifyAndReturnTrue() {
        assertTrue(target.verify(signedDataFilePath, downloadedSignature, downloadedPublicKey));
    }

    @Test
    public void verify_WrongPublicKeyInput_ShouldThrow() {
        assertThatThrownBy(() -> target.verify(signedDataFilePath, downloadedSignature, anotherPublicKey))
                .isInstanceOf(DeployActionException.class);
    }

    @Test
    public void verify_WrongSignatureInput_ShouldReturnFalse() {
        assertFalse(target.verify(signedDataFilePath, anotherSignature,downloadedPublicKey));
    }

    @Test
    public void verify_InputIsNull_ShouldThrow() {
        assertThatThrownBy(() -> target.verify(null, downloadedSignature,downloadedPublicKey))
        .isInstanceOf(IllegalArgumentException.class);
    }
}
