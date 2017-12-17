package grpc

import (
	driver "github.com/brendanball/android-remote/touchpad"
	"golang.org/x/net/context"
)

type touchpad struct {
	driver driver.Touchpad
}

func NewTouchpadServer(d driver.Touchpad) TouchpadServer {
	return &touchpad{
		driver: d,
	}
}

func (t *touchpad) Move(ctx context.Context, mr *MoveRequest) (*MoveReply, error) {
	for _, me := range mr.GetMoveEvents() {
		t.driver.Move(driver.MoveEvent{
			PointerID: me.PointerId,
			PositionX: me.PositionX,
			PositionY: me.PositionY,
			Pressure:  me.Pressure,
		})
	}
	return &MoveReply{}, nil
}
