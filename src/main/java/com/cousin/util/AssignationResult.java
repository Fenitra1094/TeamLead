package com.cousin.util;

import com.cousin.model.Assignation;
import com.cousin.model.Reservation;
import java.util.List;

public class AssignationResult {
    private List<Assignation> assignations;
    private List<Reservation> reservationsNonAssignees;

    public AssignationResult() {
    }

    public AssignationResult(List<Assignation> assignations, List<Reservation> reservationsNonAssignees) {
        this.assignations = assignations;
        this.reservationsNonAssignees = reservationsNonAssignees;
    }

    public List<Assignation> getAssignations() {
        return assignations;
    }

    public void setAssignations(List<Assignation> assignations) {
        this.assignations = assignations;
    }

    public List<Reservation> getReservationsNonAssignees() {
        return reservationsNonAssignees;
    }

    public void setReservationsNonAssignees(List<Reservation> reservationsNonAssignees) {
        this.reservationsNonAssignees = reservationsNonAssignees;
    }
}