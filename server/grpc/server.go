package grpc

import (
	"log"
	"net"

	driver "github.com/brendanball/android-remote/touchpad"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
)

func Serve(d driver.Touchpad) {
	lis, err := net.Listen("tcp", ":50051")
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	s := grpc.NewServer()

	ts := NewTouchpadServer(d)
	RegisterTouchpadServer(s, ts)
	// Register reflection service on gRPC server.
	reflection.Register(s)
	log.Println("grpc server listening on port 50051")
	if err := s.Serve(lis); err != nil {
		log.Fatalf("failed to serve: %v", err)
	}

}
