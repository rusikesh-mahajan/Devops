package com.gatepass.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GroupVisit Domain Entity Unit Tests")
public class GroupVisitTest {

    @Test
    @DisplayName("Guest Count: 5 total guests must strictly result in 4 accompanying guests")
    public void testAccompanyingGuestCalculation() {
        GroupVisit group = new GroupVisit();
        group.setTotalGuests(5);
        assertEquals(5, group.getTotalGuests());
        assertEquals(4, group.getAccompanyingGuests(), "Accompanying guests must equal totalGuests - 1");
    }

    @Test
    @DisplayName("Guest Count: 1 total guest (primary only) must result in 0 accompanying guests")
    public void testSingleGuestInGroup() {
        GroupVisit group = new GroupVisit();
        group.setTotalGuests(1);
        assertEquals(1, group.getTotalGuests());
        assertEquals(0, group.getAccompanyingGuests(), "Accompanying guests must be 0 when total is 1");
    }

    @Test
    @DisplayName("Guest Count: Total guests less than 1 must throw IllegalArgumentException")
    public void testInvalidTotalGuestsThrowsException() {
        GroupVisit group = new GroupVisit();
        assertThrows(IllegalArgumentException.class, () -> group.setTotalGuests(0));
        assertThrows(IllegalArgumentException.class, () -> group.setTotalGuests(-3));
    }

    @Test
    @DisplayName("State Transitions: PENDING group visit state rules")
    public void testPendingStateRules() {
        GroupVisit group = new GroupVisit();
        group.setStatus("PENDING");
        assertTrue(group.canApprove(), "PENDING group visit should be approvable");
        assertTrue(group.canReject(), "PENDING group visit should be rejectable");
        assertFalse(group.canCheckIn(), "PENDING group visit cannot be checked in without approval");
        assertFalse(group.canCheckOut(), "PENDING group visit cannot be checked out");
    }

    @Test
    @DisplayName("State Transitions: APPROVED group visit state rules")
    public void testApprovedStateRules() {
        GroupVisit group = new GroupVisit();
        group.setStatus("APPROVED");
        assertTrue(group.canCheckIn(), "APPROVED group visit should be eligible for check-in");
        assertFalse(group.canCheckOut(), "APPROVED group visit cannot be checked out before check-in");
        assertFalse(group.canApprove(), "Already APPROVED group visit cannot be approved again");
    }

    @Test
    @DisplayName("State Transitions: CHECKED_IN group visit state rules")
    public void testCheckedInStateRules() {
        GroupVisit group = new GroupVisit();
        group.setStatus("CHECKED_IN");
        group.setCheckInTime(LocalDateTime.now());
        assertTrue(group.canCheckOut(), "CHECKED_IN group visit should be eligible for check-out");
        assertFalse(group.canCheckIn(), "CHECKED_IN group visit cannot be checked in again (duplicate prevention)");
        assertFalse(group.canApprove(), "CHECKED_IN group visit cannot be approved");
    }

    @Test
    @DisplayName("State Transitions: CHECKED_OUT terminal state rules")
    public void testCheckedOutTerminalState() {
        GroupVisit group = new GroupVisit();
        group.setStatus("CHECKED_OUT");
        group.setCheckInTime(LocalDateTime.now().minusHours(2));
        group.setCheckOutTime(LocalDateTime.now());
        assertFalse(group.canCheckIn(), "CHECKED_OUT group visit cannot be checked in");
        assertFalse(group.canCheckOut(), "CHECKED_OUT group visit cannot be checked out again");
        assertFalse(group.canApprove(), "CHECKED_OUT group visit cannot be approved");
    }

    @Test
    @DisplayName("State Transitions: REJECTED terminal state rules")
    public void testRejectedTerminalState() {
        GroupVisit group = new GroupVisit();
        group.setStatus("REJECTED");
        assertFalse(group.canCheckIn(), "REJECTED group visit cannot be checked in");
        assertFalse(group.canCheckOut(), "REJECTED group visit cannot be checked out");
        assertFalse(group.canApprove(), "REJECTED group visit cannot be approved");
    }
}

