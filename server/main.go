package main

import (
	"log"

	"github.com/brendanball/android-remote/grpc"
	"github.com/brendanball/android-remote/touchpad"
)

func main() {
	touchpad := touchpad.Device{
		Name:         "virtual-touchpad",
		MaxPointerID: 65535,
		MaxPressure:  255,
		XProperties: touchpad.Properties{
			Max:        5000,
			Fuzz:       8,
			Resolution: 50,
		},
		YProperties: touchpad.Properties{
			Max:        4000,
			Fuzz:       8,
			Resolution: 50,
		},
	}

	if err := touchpad.Init(); err != nil {
		log.Fatal(err)
	}
	defer touchpad.Close()

	grpc.Serve(&touchpad)

}
