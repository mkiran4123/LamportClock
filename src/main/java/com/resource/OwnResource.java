package com.resource;

import java.io.Serializable;

import com.clock.LogicalClock;

import akka.actor.ActorRef;
import akka.actor.Address;
public class OwnResource extends LamportMessages implements Serializable {

	private static final long serialVersionUID = 1L;

    public OwnResource(ActorRef senderActorReference, LogicalClock timestamp) {
        super(timestamp, senderActorReference);
    }
}