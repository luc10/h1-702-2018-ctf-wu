

# May the (brute)force be with you

Challenge #2 was a bit messy. The first release was using a bugged algorithm which generates different keys for the same pin code. I thought it was specifically designed to drive everybody crazy so I dug around the code looking for hints or the flag itself.

## The revelation

A couple of days after it turns out that the challenge was unsolvable and I was like :bomb::boom::anger:.

## Our old friend Jadx

The app shows us a pin lock view:

![](https://i.imgur.com/9TpqNpy.png)

As always we need to start from Java sources:

```java
public class MainActivity extends AppCompatActivity {
    ...
    private byte[] cipherText;
    ...

    private PinLockListener mPinLockListener = new PinLockListener() {
        public void onComplete(String str) {
            String str2 = MainActivity.this.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Pin complete: ");
            stringBuilder.append(str);
            str = MainActivity.this.getKey(str);
            try {
                Log.d("DECRYPTED", new String(new SecretBox(str).decrypt("aabbccddeeffgghhaabbccdd".getBytes(), MainActivity.this.cipherText), StandardCharsets.UTF_8));
            } catch (RuntimeException e) {
                Log.d("PROBLEM", "Unable to decrypt text");
                e.printStackTrace();
            }
        }
    }

    ...

    public native byte[] getKey(String str);

    public native void resetCoolDown();

    static {
        System.loadLibrary("native-lib");
    }

    public static String bytesToHex(byte[] bArr) {
        ...
    }

    protected void onCreate(Bundle bundle) {
        ...
        this.cipherText = new Hex().decode("9646D13EC8F8617D1CEA1CF4334940824C700ADF6A7A3236163CA2C9604B9BE4BDE770AD698C02070F571A0B612BBD3572D81F99");
        ...
    }
}
```

I've redacted useless parts for analysis. What we know now:

- the cipher text is `9646D13EC8F8617D1CEA1CF4334940824C700ADF6A7A3236163CA2C9604B9BE4BDE770AD698C02070F571A0B612BBD3572D81F99`;
- **SecretBox** class is used to decipher the flag - imported from [libsodium](https://download.libsodium.org/);
- since **SecretBox** requires a **noonce** we must assume that `aabbccddeeffgghhaabbccdd` is our **noonce**;
- there're two methods imported from **libnative-lib**:
        - **getKey**;
        - **resetCoolDown**.

As you probably noticed, **getKey** method returns a byte array. The method must be fed with a **String** (which is the pin code we entered) and it returns the equivalent key.

### No Android Studio tut, please!

I thought about two solutions to this problem:

 1. write an app, embed the library, invoke **getKey** method for every possible combination (from 000000 to 999999), save each key in a file, pull the file from the emulator and try each key with another crafted tool;
        - **PROS**: time saving;
        - **CONS**: no fun :stuck_out_tongue_winking_eye:.

 2. reverse the algorithm and write a tool;
        - **PROS**: elegant solution - can be this a pro?! -, fun, fun, fun!
        - **CONS**: a lot.

Approaching the challenge with the first solution will end up with an Android Studio tutorial instead of a write-up.

## Reversing the algorithm

Don't be scared, **IDA** has a graph view that makes reversing more comfortable :sweat_smile::

![](https://i.imgur.com/Cwr6Gz1.png)

Quite huge, don't you think?!

The first part is simple:

![](https://i.imgur.com/0BxfNxL.png)

Our pin code is converted to a char[] and its pointer is kept by **r12** which is used below. The strange thing is that **gettimeofday** is called :confused:.

[gettimeofday](http://man7.org/linux/man-pages/man2/gettimeofday.2.html) returns a **timeval** struct defined as follow:

```c
struct timeval {
    time_t      tv_sec;     /* seconds */
    suseconds_t tv_usec;    /* microseconds */
};

// time_t and suseconds_t are both 8 bytes var
```

**r12** is passed to **strlen**. If the result is 0 the function is exited.

![](https://i.imgur.com/AZTfDDW.png)

After the check the code enters into a loop:

![](https://i.imgur.com/BsoL32i.png)

To convert it into c-lookalike-code:

```c
int i = 0;
int len = strlen(pin);

if (len == 0) {
  return;
}

while(...) {
    char b = pin[i % len];

    if (b - 48 == 0) {
        ...
    } else {
        ...
    }

    i++;
}
```

Here I started thinking that **tv.tv_sec** and **tv.tv_usec** were used to store bytes since they're packed into a struct.

```c
struct timeval {
    time_t      uint64;
    suseconds_t uint64;
};

// Can be considered the same as

struct timeval {
    time_t      byte[0x08];
    suseconds_t byte[0x08];
};
```

To better understand this theory I'll enter the else statement first:

![](https://i.imgur.com/FUGe0On.png)

I got it! **al** contains the original digit of our pin code for current loop (`char b = pin[i % len];`). When its raw value is bigger than 7 it's copied to fill the whole **tv_sec** field and a null terminator is added (`mov [tv.tv_usec], 0`) by setting the first byte of **tv_usec** field to **0**.

We can edit our code:

```c
int i = 0;
int len = strlen(pin);

if (len == 0) {
  return;
}

while(...) {
    char b = pin[i % len];

    if (b - 48 == 0 || b - 48 <= 7) {
        ...
    } else {
        tv.tv.sec[0] = b;
        tv.tv_sec[1] = b;
        ...
        tv.tv_sec[7] = b;
        tv.tv_usec[0] = 0;
    }

    i++;
}
```

Before getting further I'd like to write something about this little piece of code which helped me a lot:

![](https://i.imgur.com/yLndCdT.png)

This one was driving me crazy initially. A loop which appends something somewhere. At this point I thought *would you bet that this is linked to digit "9"?* and right after *would you bet that the code does the same thing for every digit except for "0" one?*.

I know that probably it sounds odd but you've to understand that every time you perform a static analysis every section of code is analyzed more and more that you start thinking to be made of bytes instead of `mov rdi, 60`  water :stuck_out_tongue_winking_eye:.

Everything the code does until here is to repeat the pin **digit** **n** times where **n** is exactly the digit raw value.
For example, if our current **digit** is "4", **tv.tv.sec** (from now I'll refer to it using **buffer**) will be filled with `4444`. If it's "5" with `55555` and so on.

Said that here you're the last part:

![](https://i.imgur.com/UWC4sZv.png)

This chunk is reached when the **raw value** is equal to **0** or after the whole flow described above. I'll make it easy:

```c
byte buffer[10];
int key[8];
int v, x, k;

int i = 0;
int k = 0;
int len = strlen(pin);

if (len == 0) {
  return;
}

while(...) {
    char b = pin[i % len];
    int n = b - 48;

    if (n == 0) {
        ...
    } else {
        for (x = 0; x < n; x++) {
            buffer[x] = b;
        }
    }

    v = 0x811c9dc5;
    for (x = 0; x < n; x++) {
        v = 0x1000193 * (v ^ buffer[x]);
    }

    key[k % 8] ^= v;
    i++; k++;
}
```

A right question will be: *what if the digit is "0"?* When it's "0" the **buffer** isn't changed meaning that the one generated by the previous digit is used.

This helps to understand that since the **buffer** is initially filled with real epoch values, our pin code can't start with "0" digits or the resulting key will not be the same for every execution. Cool, we can limit the range by 100 000 values!

**PS**: the **while** loop condition is `(i < 12)`.

## No need to reinvent the wheel

The last step requires to decipher the text. As the title says, there's no need to reinvent the wheel since [libsodium](https://download.libsodium.org/doc/bindings_for_other_languages/) has a lot of bindings.

[@Jobert](http://twitter.com/jobertabma/): if you're reading this, guess which language - your favorite - has native support? (:bulb: it starts with P).

## Challenge 2 completed - brain totally burnt out

[Here](https://github.com/luc10/h1-702-2018-ctf-wu/challenge-2) you'll find my golang implementation of this algorithm. Unfortunately, [https://play.golang.org/](https://play.golang.org/) can't run code with external dependencies so you've to install go runtimes. A good occasion to learn a new language :satisfied:.

The second flag is:

`flag{wow_yall_called_a_lot_of_func$}`
