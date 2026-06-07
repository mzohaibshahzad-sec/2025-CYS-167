package Lab_09.Mobile;

class M_P {
    void featurePT() {
        System.out.println("Prototype of mobile phone feature.");
    }
}

class GPSPhone extends M_P{
    @Override
    void featurePT() {
        System. out.println("GPS navigation and Location Track");
    }
}

class MusicPhone extends M_P {
    @Override
    void featurePT() {
        System. out.println("Music Plaxer  and Audio Playback");
    }
}

class CallingPhone extends M_P {
    @Override
    void featurePT() {
        System. out.println("Call and SMS Services");
    }
}

public class Mobile {
    public static void main(String[] args) {

        M_P phone;

        phone = new GPSPhone();
        phone.featurePT();

        phone = new MusicPhone();
        phone.featurePT();

        phone = new CallingPhone();
        phone.featurePT();
    }
}
