package Roles;

public class Rooms {
    private int roomNumber;
    private boolean occupied;
    private int patientId;

    public Rooms(int roomNumber, boolean occupied, int patientId) {
        this.roomNumber = roomNumber;
        this.occupied = occupied;
        this.patientId = patientId;
    }

    public int getRoomNumber() { return roomNumber; }
    public boolean isOccupied() { return occupied; }
    public int getPatientId() { return patientId; }

    public void setOccupied(boolean occupied) { this.occupied = occupied; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    @Override
    public String toString() {
        return "Room: " + roomNumber + " | Status: " +
                (occupied ? "Occupied" : "Available") +
                " | Patient ID: " + (patientId == 0 ? "None" : patientId);
    }
}