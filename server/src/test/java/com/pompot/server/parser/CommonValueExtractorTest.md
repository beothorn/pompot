# CommonValueExtractorTest

Verifies that the extractor reports repeated values across parsed pom graphs.
The test creates two `ParsedPom` instances with matching property and
dependency values and asserts that only those repeated entries appear in the
result.
