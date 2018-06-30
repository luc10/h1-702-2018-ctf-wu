
# Once upon a time

I was scrolling tweets on my phone when I noticed a [tweet](https://twitter.com/ITSecurityguard/status/1006299036318687238) posted by [Patrik Fehrenbach](https://twitter.com/ITSecurityguard). Yes, someone's tried to inject an XSS payload but the most important thing was that [h1](http://hackerone.com/) will run another great **CTF** challenge.

Even if I was/am busy with exams I decided to jump into (my first ever **CTF** challenge) Android one.

### Let the fun begin

The first challenge is a static analysis one. Since an *apk* is a valid *zip* archive we can extract the whole content and browse files with a simple command:

```bash
$ unzip challenge1_release.apk -d challenge1 && open $_
```

From this moment until my last dot I'll mainly use two tools:

- [IDA](https://www.hex-rays.com/products/ida/support/download_freeware.shtml) (which comes in a free version too);
- [JADX](https://github.com/skylot/jadx).

Once loaded the *apk* (not only the *dex* file - afterward, I'll tell you why) file into **jadx** we'll notice that there're 4 total classes:

- BuildConfig;
- **MainActivity**;
- **FourthPart**;
- **R**;

I've highlighted the interesting ones.

The **MainActivity** one contains the following code:

```java
    ...

    public native void oneLastThing();

    public native String stringFromJNI();

    static {
        System.loadLibrary("native-lib");
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_main);
        ((TextView) findViewById(R.id.sample_text)).setText("Reverse the apk!");
        doSomething();
    }

    void doSomething() {
        Log.d("Part 1", "The first part of your flag is: \"flag{so_much\"");
    }

    ...
```

The function named **doSomething** contains the first part of our flag (which is `flag{so_much`) as **FourthPart** class contains the fourth one:

```java
public class FourthPart {
    String eight()     { return "w"; }
    String five()     { return "_"; }
    String four()     { return "h"; }
    String one()     { return "m"; }
    String seven()     { return "o"; }
    String six()     { return "w"; }
    String three()     { return "c"; }
    String two()     { return "u"; }
}
```

By imaginary calling each method from **one** to **eight**, we obtain `much_wow`.

Time to check if there's another flag part into our dex file:

```bash
$ strings classes.dex | grep -i "part"

getParticipant
getParticipants
mParticipant
mParticipants
&notification_template_part_chronometer
notification_template_part_time
part_3 <-----
participants
```

Indeed there's but it's a reference to a string located into a resource named **strings.xml** (located under *resources.arsc/values/* - that's why I told you to open the whole apk with jdax).

Now we have the third chunk too -> `analysis_`.

### NDK - N_otyet_D_one_K?

Android let developers build native libraries and bind them directly to Java code using what they call **NDK** and since there're references to external functions we need to import **lib-nativelib** into **IDA** which once loaded will reveal the second part: `_static_` under the **stringFromJNI** function body.

![](https://i.imgur.com/YaGp4z8.png)

From java code we noticed there's **oneLastThing** method too:

```java
...
public native void oneLastThing();
...
```

![](https://i.imgur.com/zWnck7q.png)

I've to be honest. At this time I felt like :confused:... It took me a while to notice that there were other exported functions:

![](https://i.imgur.com/bHMLaDg.png)

Each one contains a single char that chained to others compose the last part of the flag: `_and_cool}`.

### My first ever flag

Finally we have the full flag:

`flag{so_much_static_analysis_much_wow_and_cool}`
