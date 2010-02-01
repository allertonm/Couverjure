/*
    Copyright 2010 Mark Allerton. All rights reserved.

    Redistribution and use in source and binary forms, with or without modification, are
    permitted provided that the following conditions are met:

       1. Redistributions of source code must retain the above copyright notice, this list of
          conditions and the following disclaimer.

       2. Redistributions in binary form must reproduce the above copyright notice, this list
          of conditions and the following disclaimer in the documentation and/or other materials
          provided with the distribution.

    THIS SOFTWARE IS PROVIDED BY MARK ALLERTON ``AS IS'' AND ANY EXPRESS OR IMPLIED
    WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
    FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
    CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
    SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
    ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
    ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

    The views and conclusions contained in the software and documentation are those of the
    authors and should not be interpreted as representing official policies, either expressed
    or implied, of Mark Allerton.
*/

package org.couverjure.core;

import com.sun.jna.Pointer;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The ID class extends JNA's Pointer to add functions needed for dealing with Objective-C 'id' pointers.
 * We subclass rather than aggregate here because this allows two useful things:
 * 1) an ID to be passed to any method requiring a Pointer
 * 2) It allows us access to the Pointer's 'peer' member, which we need to be able to coerce the pointer
 * to a scalar - something that is a requirement for handling objc_msgSend return values.
 */
public class ID extends Pointer {
    /**
     * We use this map to avoid creating more than one object to manage the same native ID
     * the effect of using <id> as the key and WeakReference<id> as the value is to
     * create a queryable weak hashset of IDs
     */
    private final static Map<ID, WeakReference<ID>> managedIDs = new WeakHashMap<ID, WeakReference<ID>>();

    /**
     * This flag says whether the finalizer should call CFRelease on the native ID
     */
    private boolean releaseOnFinalize = false;
    /**
     * Tracks whether we have been retained already or not - we only need to retain at most once on the
     * java/clojure side of the fence.
     */
    private boolean retained = false;

    /**
     * The public constructor, used by the typemapper, can only create IDs with releaseOnFinalize == false
     * This is because not everything passed as ID is in fact a pointer we should call CFRelease on
     * - in particular for objc_msgSend it could be used to pass scalars, C-string pointers and class pointers.
     *
     * It is the responsibility of the code (in couverjure.core) that receives the ID to work out based
     * on context how the ID is being used - and if it is being used as an ObjC object reference either
     * releaseOnFinalize or retainAndReleaseOnFinalize should be called to obtain a reference that will
     * release the native object on finalization.
     * @param peer the native address
     */
    public ID(long peer) {
        super(peer);
    }

    /**
     * This private ctor is used to create ID instances with releaseOnFinalize set. This is done via
     * the releaseOnFinalize method on an existing (assumed non-releaseOnFinalize) ID object.
     * @param peer
     * @param releaseOnFinalize
     */
    private ID(long peer, boolean releaseOnFinalize) {
        super(peer);
        this.releaseOnFinalize = releaseOnFinalize;
    }

    /**
     * Obtains a release-on-finalize ID reference for the given ID
     * This method attempts to reuse existing releaseable IDs, managing a weakly referenced set of
     * existing objects. This allows us to avoid multiple retains on objects, for example when
     * a method is invoked many times between GCs - which would in turn lead to multiple releases
     * at GC time. We're doing this at the expense of having a synchronization point (on the weak set)
     * on every marshalling of an object reference from native to clojure.
     * Time will tell if I've chosen the right side of this tradeoff :)
     * @return a release-on-finalize ID
     */
    public ID releaseOnFinalize() {
        if (Core.DEBUG) System.out.println("ID.releaseOnFinalize: " + this);

        if (this.releaseOnFinalize) return this;

        // this will be a very good candidate for a RW lock in future, I'm sure.
        synchronized (managedIDs) {
            // we rely on our base class' (JNA Pointer) implementing equals and hashcode appropriately
            WeakReference<ID> wref = managedIDs.get(this);
            ID releaseOnFinalizeID = wref != null ? wref.get() : null;
            if (releaseOnFinalizeID == null) {
                // no existing releaseOnFinalize ID, create a new one
                releaseOnFinalizeID = new ID(peer, true);
                managedIDs.put(releaseOnFinalizeID, new WeakReference<ID>(releaseOnFinalizeID));
                if (Core.DEBUG) System.out.println("ID.releaseOnFinalizeID new " + releaseOnFinalizeID);
            } else {
                if (Core.DEBUG) System.out.println("ID.releaseOnFinalizeID reuse " + releaseOnFinalizeID);
            }
            return releaseOnFinalizeID;
        }
    }

    /**
     * retainAndReleaseOnFinalize does a releaseOnFinalize and then retains (at most once) the result
     * @return a retained release-on-finalize ID
     */
    public ID retainAndReleaseOnFinalize() {
        ID releaseOnFinalizeID = releaseOnFinalize();
        return releaseOnFinalizeID.retain();
    }

    /**
     * Coerce this ID to boolean
     * @return ID coerced to boolean
     */
    public boolean asBoolean() {
        checkReleaseOnFinalize("asBoolean", false);
        return peer != 0;
    }

    /**
     * Coerce this ID to byte
     * @return ID coerced to byte
     */
    public byte asByte() {
        checkReleaseOnFinalize("asByte", false);
        return (byte) peer;
    }

    /**
     * Coerce this ID to int
     * @return ID coerced to int
     */
    public int asInt() {
        checkReleaseOnFinalize("asInt", false);
        return (int) peer;
    }

    /**
     * Coerce this ID to short
     * @return ID coerced to short
     */
    public short asShort() {
        checkReleaseOnFinalize("asShort", false);
        return (short) peer;
    }

    /**
     * Coerce this ID to long
     * @return ID coerced to short
     */
    public long asLong() {
        checkReleaseOnFinalize("asLong", false);
        return peer;
    }

    /**
     * This is here to allow our TypeMapper to get access to the native address
     * @return the native address
     */
    long getAddress() {
        return peer;
    }

    /**
     * Internal method to retain a releaseOnFinalize ID
     * @return
     */
    private synchronized ID retain() {
        checkReleaseOnFinalize("retain", true);
        if (!retained) {
            if (Core.DEBUG) System.out.println(String.format("ID.retain %s", this));
            Core.CORE.foundation.CFRetain(this);
            retained = true;
        }
        return this;
    }

    /**
     * Handle finalization
     * @throws Throwable
     */
    public void finalize() throws Throwable {
        if (releaseOnFinalize) {
            if (Core.DEBUG) System.out.println(String.format("ID.finalize %s", this));
            Core.CORE.foundation.CFRelease(this);
        }
        super.finalize();
    }

    /**
     * Outputs a useful-for-debugging string representation
     * @return string representing this ID
     */
    public String toString() {
        return String.format("%s  releaseOnFinalize: %b retained: %b", super.toString(), releaseOnFinalize, retained);
    }

    /**
     * This method is used to check whether operations are valid given the state of releaseOnFinalize
     * - i.e you cannot coerce when releaseOnFinalize is true, and you cannot retain if not.
     * @param name
     * @param releaseOnFinalize
     */
    private void checkReleaseOnFinalize(String name, boolean releaseOnFinalize) {
        if (this.releaseOnFinalize != releaseOnFinalize) {
            throw new IllegalStateException(
                    String.format("Can't do ID.%s with releaseOnFinalize=%b", name, this.releaseOnFinalize));
        }
    }
}
