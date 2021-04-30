package com.resource;

import akka.actor.ActorRef;
import akka.actor.Address;

import com.clock.LogicalClock;


import java.io.Serializable;

public abstract class LamportMessages implements Comparable<LamportMessages>, Serializable {

    private static final long serialVersionUID = 1L;
    private final ActorRef senderActorReference;
    private final LogicalClock timestamp;

    LamportMessages(LogicalClock timestamp, ActorRef senderActorReference) {
        this.senderActorReference = senderActorReference;
        this.timestamp = timestamp;
    }

    public ActorRef senderActorReference() {
        return senderActorReference;
    }

    public LogicalClock getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(LamportMessages other) {
        int clockComparison = timestamp.compareTo(other.timestamp);

        // Tie breaker
        if (clockComparison == 0) {
            final int h1 = senderActorReference.hashCode(), h2 = other.senderActorReference.hashCode();
            if (h1 < h2) return -1;
            if (h1 > h2) return 1;
            throw new RuntimeException();
        }

        return clockComparison;
    }
}
