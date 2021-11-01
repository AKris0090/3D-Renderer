/*
 * Copyright (c) 2021.
 *
 * Arjun Krishnan 10/31/2021
 * See my other coding projects at: akrishnan.netlify.app
 * Questions, email me at: artk0090@gmail.com
 */

public class s {
    public static void main(String[] args) {
        String test = "a0a1a1a0a0a1a0a0a a0a1a0a1a0a1a1a1a a0a1a1a0a0a1a0a0a a0a1a1a0a1a1a1a0a a0a1a0a1a1a0a0a1a a0a0a1a1a0a0a1a0a a0a1a0a1a1a0a0a1a a0a0a1a1a0a1a1a0a a0a1a0a0a1a1a0a0a a0a1a1a1a1a0a0a1a a0a0a1a1a1a0a0a1a a0a1a1a1a0a1a1a0a a0a1a1a0a0a1a0a0a a0a1a1a0a1a1a0a1a a0a1a1a0a0a0a1a1a a0a1a1a1a0a1a0a1a a0a1a1a0a0a1a0a1a a0a1a0a1a0a1a1a1a a0a1a1a1a0a1a1a1a a0a1a1a1a0a1a1a0a a0a1a1a0a0a0a1a1a a0a0a1a1a0a0a1a1a a0a1a0a1a0a1a1a0a a0a1a1a0a1a1a0a1a a0a1a1a0a0a0a1a0a a0a1a1a0a1a1a1a0a a0a1a1a0a1a1a0a0a a0a0a1a1a0a1a1a0a a0a1a0a1a1a0a0a1a a0a1a1a0a1a1a0a1a a0a1a0a1a1a0a1a0a a0a1a1a0a1a1a1a0a a0a1a0a1a1a0a1a0a a0a0a1a1a0a0a1a1a a0a1a0a1a0a1a1a0a a0a1a1a1a1a0a0a1a a0a1a0a1a1a0a1a0a a0a1a0a1a1a0a0a0a a0a1a0a0a1a0a0a1a a0a0a1a1a1a1a0a1";
        while (test.contains("a")){
            int index = test.indexOf('a');
            test = test.substring(0, index) + test.substring(index + 1);
        }
        System.out.println(test);
    }
}
