pragma solidity ^0.4.24;
function addressTest()view returns(bytes32 addressValue) {
     assembly{
            let x := mload(0x40)  //Find empty storage location using "free memory pointer"
            mstore(x,address) //Place current contract address
            addressValue := mload(x)
        }
    }