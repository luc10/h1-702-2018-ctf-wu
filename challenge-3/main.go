package main

import (
	"fmt"
	"strings"
	"encoding/hex"
)

// Holds our key
var key = []byte {
	't', 'h', 'i', 's', '_', 'i', 's', '_', 'a', '_', 'k', '3', 'y',
}

var substitutions = map[string]string {
	"O": "0",
	"t": "7",
	"B": "8",
	"z": "a",
	"F": "f",
	"k": "e",
}

func reverse(s string) (o string) {
	for _, c := range s {
		o = string(c) + o
	}

	return
}

func decrypt(b []byte) (o string) {
	for i, c := range b {
		o += string(c ^ key[i % len(key)])
	}

	return
}

func main() {
	ct := "kO13t41Oc1b2z4F5F1b2BO33c2d1c61OzOdOtO"

	// Reverse the string
	ct = reverse(ct)

	// Replace chars
	for o, n := range substitutions {
		ct = strings.Replace(ct, o, n, -1)
	}

	b, _ := hex.DecodeString(ct)
	fmt.Printf("flag{%s}\n", decrypt(b))
}
