# Super Duo

This lesson consists of two given projects containing some issues. Also given are some complaints of users and the general task to improve the UX and the accessibility.

Both apps aren't architectured very well and I did my best to improve the architecture by leaving the underlying basic concepts intact.

One such issue that both projects had has been naked, manual JSON http request handling which I replaced using [Retrofit](https://github.com/square/retrofit). This greatly improves the readability and is propably less error prone.