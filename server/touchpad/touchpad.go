package touchpad

import (
	// #cgo LDFLAGS: -levdev
	// #cgo CFLAGS: -I/usr/include/libevdev-1.0/libevdev  -I/usr/include/libevdev-1.0
	// #include <linux/input.h>
	// #include <libevdev/libevdev-uinput.h>
	// #include <unistd.h>
	"C"
)
import (
	"log"
	"runtime/debug"
	"unsafe"
)

type Touchpad interface {
	Move(e MoveEvent) error
}

type Device struct {
	Name         string
	MaxPointerID int
	MaxPressure  int
	XProperties  Properties
	YProperties  Properties
	uidev        *C.struct_libevdev_uinput
	dev          *C.struct_libevdev
}

var _ Touchpad = &Device{}

type Properties struct {
	Max        int
	Resolution int
	Fuzz       int
}

func (d *Device) Init() error {
	var (
		mtSlotAbsinfo = unsafe.Pointer(&C.struct_input_absinfo{
			maximum: 15,
		})
		mtTrackIDAbsinfo = unsafe.Pointer(&C.struct_input_absinfo{
			maximum: C.__s32(d.MaxPointerID),
		})
		mtPosXAbsinfo = unsafe.Pointer(&C.struct_input_absinfo{
			maximum:    C.__s32(d.XProperties.Max),
			fuzz:       C.__s32(d.XProperties.Fuzz),
			resolution: C.__s32(d.XProperties.Resolution),
		})
		mtPosYAbsinfo = unsafe.Pointer(&C.struct_input_absinfo{
			maximum:    C.__s32(d.YProperties.Max),
			fuzz:       C.__s32(d.YProperties.Fuzz),
			resolution: C.__s32(d.YProperties.Resolution),
		})
		mtPressureAbsinfo = unsafe.Pointer(&C.struct_input_absinfo{
			maximum: 255,
		})
	)
	dev := C.libevdev_new()
	// d.dev = dev
	C.libevdev_set_name(dev, C.CString(d.Name))
	check(C.libevdev_enable_event_type(dev, C.EV_ABS))
	check(C.libevdev_enable_event_code(dev, C.EV_ABS, C.ABS_MT_SLOT, mtSlotAbsinfo))
	check(C.libevdev_enable_event_code(dev, C.EV_ABS, C.ABS_MT_TRACKING_ID, mtTrackIDAbsinfo))
	check(C.libevdev_enable_event_code(dev, C.EV_ABS, C.ABS_MT_POSITION_X, mtPosXAbsinfo))
	check(C.libevdev_enable_event_code(dev, C.EV_ABS, C.ABS_MT_POSITION_Y, mtPosYAbsinfo))
	check(C.libevdev_enable_event_code(dev, C.EV_ABS, C.ABS_MT_PRESSURE, mtPressureAbsinfo))
	check(C.libevdev_enable_event_type(dev, C.EV_KEY))
	check(C.libevdev_enable_event_code(dev, C.EV_KEY, C.BTN_TOUCH, nil))

	cerr := C.libevdev_uinput_create_from_device(dev, C.LIBEVDEV_UINPUT_OPEN_MANAGED, &d.uidev)
	if cerr != 0 {
		log.Fatalf("error: %v", cerr)
	}
	return nil
}

func (d *Device) Close() error {
	C.libevdev_uinput_destroy(d.uidev)
	C.libevdev_free(d.dev)
	return nil
}

type MoveEvent struct {
	PointerID int32
	PositionX int32
	PositionY int32
	Pressure  int32
}

func (d *Device) Move(e MoveEvent) error {
	check(C.libevdev_uinput_write_event(d.uidev, C.EV_ABS, C.ABS_MT_TRACKING_ID, C.int(e.PointerID)))
	check(C.libevdev_uinput_write_event(d.uidev, C.EV_ABS, C.ABS_MT_POSITION_X, C.int(e.PositionX)))
	check(C.libevdev_uinput_write_event(d.uidev, C.EV_ABS, C.ABS_MT_POSITION_Y, C.int(e.PositionY)))
	check(C.libevdev_uinput_write_event(d.uidev, C.EV_ABS, C.ABS_MT_PRESSURE, C.int(e.Pressure)))
	check(C.libevdev_uinput_write_event(d.uidev, C.EV_SYN, C.SYN_REPORT, 0))
	return nil
}

func check(err C.int) {
	if err != 0 {
		debug.PrintStack()
		log.Fatal(err)
	}
}
