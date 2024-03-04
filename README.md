Kad3
========
This is an implementation of Kademlia/Mainline DHT [Wikipedia Kademlia Link](http://en.wikipedia.org/wiki/Kademlia) and [Wikipedia Mainline Link](https://en.wikipedia.org/wiki/Mainline_DHT) this implementation was refrenced from [Stanford Paper](https://codethechange.stanford.edu/guides/guide_kademlia.html)

What is a DHT
-----
A DHT (Distributed Hash Table) is a distributed network that uses an XOR based routing system based off of user defined UIDs. UIDs are made using CRC32c hash of the users IP address and a random number. The random number allows multiple nodes on 1 NAT, while also limiting any person up to 100 nodes per NAT to stop 50% attacks.

> [!NOTE]
> Decentralized networks work on a teir based system meaning that some nodes have more power or say than others do.

> [!IMPORTANT]
> This is a DHT implementation, NOT a torrent implementation, this can talk with BitTorrent but it can only do FIND_NODE, PING, GET, & PUT.

Features
-----
- [x] PING
- [x] FIND_NODE
- [x] GET
- [x] PUT
- [x] Same protocol as BitTorrent (Easy Torrent Implementation)
- [x] No external Libraries
- [x] Option for MainLine or Kademlia
- [x] Teredo support
- [x] Consensus IP defining
- [x] Bogon detecting
- [x] Spam Throttling
- [x] Secure ID only option

| BEP | Title | Status |
|------|-------|--------|
|[BEP5](http://bittorrent.org/beps/bep_0005.html)|Bittorrent DHT| Yes |
|[BEP32](http://bittorrent.org/beps/bep_0032.html)|IPv6| Yes |
|[BEP42](http://www.bittorrent.org/beps/bep_0042.html)|DHT Security Extension| Yes |

Examples
-----
To join a node you can do
```java
Kademlia k = new Kademlia();
k.join(6881, InetAddress.getByName("HOSENAME"), 6881);
```

To start a node without joining you can do
```java
Kademlia k = new Kademlia();
k.bind(6881);
```

To set Kademlia to Mainline you can do:
```java
Kademlia k = new Kademlia("MainLine");
```

Requirements
-----
Java > 8
