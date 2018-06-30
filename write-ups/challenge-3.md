
# Deodex is not for armpits

~ While solving the 3rd challenge I was thinking to a deodorant applicator all the time ~

Said that we can get down to business.

## Optimized DEX

ODEX stands for Optimized DEX. Basically, they are memory optimized applications/application components. We need to grab two tools:

- [backsmali](https://bitbucket.org/JesusFreke/smali/downloads/baksmali-2.2.4.jar);
- [smali](https://bitbucket.org/JesusFreke/smali/downloads/smali-2.2.4.jar).

We'll use the first one to extract smali files from odex and the second one to build a dex file starting from these.

## Terminal, wake up!

It's time to launch some commands:

``` bash
$ cd working_dir
$ unzip challenge3_release.apk -d challenge3 && cd $_
$ wget https://bitbucket.org/JesusFreke/smali/downloads/baksmali-2.2.4.jar
$ wget https://bitbucket.org/JesusFreke/smali/downloads/smali-2.2.4.jar
$ java -jar baksmali-2.2.4.jar x -o deodexed -b boot.oat base.odex
$ java -jar smali-2.2.4.jar a deodexed -o classes.dex
```

We can now feed **jadx** with a dex file.

## Your first symmetric cipher

Have you ever developed a symmetric cipher using a **^** (xor) operator? I did it! The challenge uses a simple xor cipher and a key which is `this_is_a_k3y`:

```java
    private static char[] key = new char[]{'t', 'h', 'i', 's', '_', 'i', 's', '_', 'a', '_', 'k', '3', 'y'};

    public static byte[] hexStringToByteArray(String str) {
        int length = str.length();
        byte[] bArr = new byte[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bArr;
    }

    public static boolean checkFlag(String str) {
        ...
        String encryptDecrypt =
            encryptDecrypt(key, hexStringToByteArray(new StringBuilder("kO13t41Oc1b2z4F5F1b2BO33c2d1c61OzOdOtO")
                .reverse()
                .toString()
                .replace("O", "0")
                .replace("t", "7")
                .replace("B", "8")
                .replace("z", "a")
                .replace("F", "f")
                .replace("k", "e")));
        ...
    }

    private static String encryptDecrypt(char[] cArr, byte[] bArr) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bArr.length; i++) {
            stringBuilder.append((char) (bArr[i] ^ cArr[i % cArr.length]));
        }
        return stringBuilder.toString();
    }
```

We've just to reverse the string `kO13t41Oc1b2z4F5F1b2BO33c2d1c61OzOdOtO` and globally replace chars:

- **O** with **0** (zero);
- **t** with **7**;
- **B** with **8**;
- **z** with **a**;
- **F** with **f** (useless);
- **k** with **e**.

This ends up in a hex string which is converted to a byte array and passed to **encryptDecrypt** method.

## My second love: GoLang

First one is reverse engineering :stuck_out_tongue_winking_eye:
[GoLang](https://golang.org/) has basically everything you need to solve the problem except for a **strings.reverse** method. No, that one has never been added as a native method...

[Here](https://github.com/luc10/h1-702-2018-ctf-wu/challenge-3/) you'll find the code to decipher the flag.

If you don't want to install golang you can copy, paste and run the code [here](https://play.golang.org/). Pretty cool, right?

The third flag is:

`flag{secr3t_littl3_th4ng}`
