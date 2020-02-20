package com.bot.voice;
import com.sedmelluq.lava.extensions.youtuberotator.planner.AbstractRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.tools.Tuple;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.IpBlock;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public final class MyRotatingIpPlanner extends AbstractRoutePlanner {
    private static final Logger log = LoggerFactory.getLogger(com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingIpRoutePlanner.class);
    private static final Random random = new Random();
    private final Predicate<InetAddress> ipFilter;
    private final AtomicBoolean next;
    private final AtomicReference<BigInteger> rotateIndex;
    private final AtomicReference<BigInteger> index;
    private volatile InetAddress lastFailingAddress;

    public MyRotatingIpPlanner(List<IpBlock> ipBlocks) {
        this(ipBlocks, (i) -> {
            return true;
        });
    }

    public MyRotatingIpPlanner(List<IpBlock> ipBlocks, Predicate<InetAddress> ipFilter) {
        this(ipBlocks, ipFilter, true);
    }

    public MyRotatingIpPlanner(List<IpBlock> ipBlocks, Predicate<InetAddress> ipFilter, boolean handleSearchFailure) {
        super(ipBlocks, handleSearchFailure);
        this.ipFilter = ipFilter;
        this.next = new AtomicBoolean(false);
        this.rotateIndex = new AtomicReference(BigInteger.valueOf(0L));
        this.index = new AtomicReference(BigInteger.valueOf(0L));
        this.lastFailingAddress = null;
    }

    public void next() {
        this.rotateIndex.accumulateAndGet(BigInteger.ONE, BigInteger::add);
        if (!this.next.compareAndSet(false, true)) {
            log.warn("Call to next() even when previous next() hasn't completed yet");
        }

    }

    public InetAddress getCurrentAddress() {
        return ((BigInteger)this.index.get()).compareTo(BigInteger.ZERO) == 0 ? null : this.ipBlock.getAddressAtIndex(((BigInteger)this.index.get()).subtract(BigInteger.ONE));
    }

    public BigInteger getIndex() {
        return (BigInteger)this.index.get();
    }

    public BigInteger getRotateIndex() {
        return (BigInteger)this.rotateIndex.get();
    }

    protected Tuple<InetAddress, InetAddress> determineAddressPair(Tuple<Inet4Address, Inet6Address> remoteAddresses) throws HttpException {
        InetAddress currentAddress = null;
        InetAddress remoteAddress;
        if (this.ipBlock.getType() == Inet4Address.class) {
            if (remoteAddresses.l == null) {
                throw new HttpException("Could not resolve host");
            }

            if (((BigInteger)this.index.get()).compareTo(BigInteger.ZERO) == 0 || this.next.get()) {
                currentAddress = this.extractLocalAddress();
                log.info("Selected " + currentAddress.toString() + " as new outgoing ip");
            }

            remoteAddress = (InetAddress)remoteAddresses.l;
        } else {
            if (this.ipBlock.getType() != Inet6Address.class) {
                throw new HttpException("Unknown IpBlock type: " + this.ipBlock.getType().getCanonicalName());
            }

            if (remoteAddresses.r != null) {
                if (((BigInteger)this.index.get()).compareTo(BigInteger.ZERO) == 0 || this.next.get()) {
                    currentAddress = this.extractLocalAddress();
                    log.info("Selected " + currentAddress.toString() + " as new outgoing ip");
                }

                remoteAddress = (InetAddress)remoteAddresses.r;
            } else {
                if (remoteAddresses.l == null) {
                    throw new HttpException("Could not resolve host");
                }

                remoteAddress = (InetAddress)remoteAddresses.l;
                log.warn("Could not look up AAAA record for host. Falling back to unbalanced IPv4.");
            }
        }

        if (currentAddress == null && ((BigInteger)this.index.get()).compareTo(BigInteger.ZERO) > 0) {
            currentAddress = this.ipBlock.getAddressAtIndex(((BigInteger)this.index.get()).subtract(BigInteger.ZERO));
        }

        this.next.set(false);
        return new Tuple(currentAddress, remoteAddress);
    }

    protected void onAddressFailure(InetAddress address) {
        if (this.lastFailingAddress != null && this.lastFailingAddress.toString().equals(address.toString())) {
            log.warn("Address {} was already failing, not triggering next()", address.toString());
        } else {
            this.lastFailingAddress = address;
            this.next();
        }
    }

    private InetAddress extractLocalAddress() {
        long triesSinceBlockSkip = 0L;
        BigInteger it = BigInteger.valueOf(0L);

        InetAddress localAddress;
        do {
            if (this.ipBlock.getSize().multiply(BigInteger.valueOf(2L)).compareTo(it) < 0) {
                throw new RuntimeException("Can't find a free ip");
            }

            if (this.ipBlock.getSize().compareTo(BigInteger.valueOf(128L)) > 0) {
                this.index.accumulateAndGet(BigInteger.valueOf((long)(random.nextInt(10) + 10)), BigInteger::add);
            } else {
                this.index.accumulateAndGet(BigInteger.ONE, BigInteger::add);
            }

            it = it.add(BigInteger.ONE);
            ++triesSinceBlockSkip;
            if (this.ipBlock.getSize().compareTo(Ipv6Block.BLOCK64_IPS) > 0 && triesSinceBlockSkip > 128L) {
                triesSinceBlockSkip = 0L;
                this.rotateIndex.accumulateAndGet(Ipv6Block.BLOCK64_IPS.add(BigInteger.valueOf(random.nextLong())), BigInteger::add);
            }

            try {
                localAddress = this.ipBlock.getAddressAtIndex(((BigInteger)this.index.get()));
            } catch (Exception var6) {
                this.index.set(BigInteger.ZERO);
                localAddress = null;
            }
        } while(localAddress == null || !this.ipFilter.test(localAddress) || !this.isValidAddress(localAddress));

        return localAddress;
    }
}
