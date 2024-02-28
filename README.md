Kad3
========
This is an implementation of Kademlia/Mainline DHT [Wikipedia Kademlia Link](http://en.wikipedia.org/wiki/Kademlia) and [Wikipedia Mainline Link](https://en.wikipedia.org/wiki/Mainline_DHT) this implementation was refrenced from [Stanford Paper](https://codethechange.stanford.edu/guides/guide_kademlia.html)

What is a DHT
-----
A DHT (Distributed Hash Table) is a distributed network that uses an XOR based routing system based off of user defined UIDs. UIDs are made using CRC32c hash of the users IP address and a random number. The random number allows multiple nodes on 1 NAT, while also limiting any person up to 100 nodes per NAT to stop 50% attacks.

> [!NOTE]
> Decentralized networks work on a teir based system meaning that some nodes have more power or say than others do.

> [!IMPORTANT]
> THIS IS NOT FINISHED YET!

Features
-----
- [x] PING
- [x] FIND_NODE
- [x] Same protocol as BitTorrent (Easy Torrent Implementation)
- [x] No external Libraries
- [x] Option for MainLine or Kademlia

| BEP | Title | Status |
|------|-------|--------|
|[BEP5](http://bittorrent.org/beps/bep_0005.html)|Bittorrent DHT| Yes |
|[BEP32](http://bittorrent.org/beps/bep_0032.html)|IPv6| Yes |
|[BEP42](http://www.bittorrent.org/beps/bep_0042.html)|DHT Security Extension| Yes |

Requirements
-----
Java > 8
