package main

import (
	"fmt"
	"strings"
	"encoding/binary"
	"encoding/hex"
	"golang.org/x/crypto/nacl/secretbox"
)

func genValidPinCodes() (codes []string) {
	for c := 100000; c <= 999999; c++ {
		codes = append(codes, fmt.Sprintf("%6d", c))
	}

	return
}

func keyFromPinCode(code string) []byte {
	var (
		xorArray  []byte
		key       []byte
		values    [8]uint32
	  	v         uint32
	)

	x := 0
	for i := 0; i < 12; i++ {
		// Get a digit from pin code string and switch on
		// its respective number value
		c := code[i % len(code)]
		n := c - 0x30

		if n != 0 {
			xorArray = []byte(strings.Repeat(string(c), int(n)))
		}

		v = 0x811c9dc5
		for _, b := range xorArray {
			v = 0x1000193 * (v ^ uint32(b))
		}

		values[x % 8] ^= uint32(v)

		x++
	}

	key = make([]byte, 32)
	for i, value := range values {
		binary.LittleEndian.PutUint32(key[i * 4:], uint32(value))
	}

	return key
}


func main() {
	box, _ := hex.DecodeString("9646d13ec8f8617d1cea1cf4334940824c700adf6a7a3236163ca2c9604b9be4bde770ad698c02070f571a0b612bbd3572d81f99")
	noonce := [24]byte{}
	key := [32]byte{}

	copy(noonce[:], []byte("aabbccddeeffgghhaabbccdd"))
	for _, code := range genValidPinCodes() {
		copy(key[:], keyFromPinCode(code))

		if plain, done := secretbox.Open(nil, box, &noonce, &key); done {
			fmt.Printf("Flag found with pin '%s': \n%s\n", code, string(plain))
		}
	}
}
