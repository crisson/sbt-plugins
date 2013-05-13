package com.untyped.sbtassets

import sbt._

class RulesSpec extends BaseSpec {

  def assertChange(file: File)(fn: => Any) = {
    val ts0 = file.lastModified
    fn
    val ts1 = file.lastModified
    ts1 must be > (ts0)
  }

  def assertNoop(file: File)(fn: => Any) = {
    val ts0 = file.lastModified
    fn
    val ts1 = file.lastModified
    ts0 must equal (ts1)
  }

  def assertExists(file: File) =
    file.exists must equal (true)

  def assertNotExists(file: File) =
    file.exists must equal (false)

  def assertContents(file: File)(content: => String) =
    IO.read(file) must equal (content)

  // Tests --------------------------------------

  describe("Cat") {
    val inDir      = createTemporaryFiles(
                       "a.txt" -> "a",
                       "b.txt" -> "b",
                       "c.txt" -> "c"
                     )
    val inSources  = Selectors.Const(List(
                       Source(Path("/a"), inDir / "a.txt", Nil),
                       Source(Path("/b"), inDir / "b.txt", Nil),
                       Source(Path("/c"), inDir / "c.txt", Nil)
                     ))
    val outDir     = IO.createTemporaryDirectory
    val outSources = Selectors.Const(List(
                       Source(Path.Root, outDir / "out.txt", Nil)
                     ))
    val rule       = Rules.Cat(outDir / "out.txt", inSources)

    it("should return sources") {
      rule.sources must equal (outSources.sources)
      assertNotExists(outDir / "out.txt")
    }

    it("should compile sources") {
      assertNotExists(outDir / "out.txt")
      assertChange(outDir / "out.txt") {
        rule.compile()
      }
      assertContents(outDir / "out.txt") { "abc" }
      assertNoop(outDir / "out.txt") {
        rule.compile()
      }
    }
  }

  describe("Coffee") {
    val inDir      = createTemporaryFiles(
                       "a.coffee" -> "alert 'a'",
                       "b.coffee" -> "alert 'b'",
                       "c.coffee" -> "alert 'c'"
                     )
    val inSources  = Selectors.Const(List(
                       Source(Path("/a"), inDir / "a.coffee", Nil),
                       Source(Path("/b"), inDir / "b.coffee", Nil),
                       Source(Path("/c"), inDir / "c.coffee", Nil)
                     ))
    val outDir     = IO.createTemporaryDirectory
    val outSources = Selectors.Const(List(
                       Source(Path("/a"), outDir / "a.js", Nil),
                       Source(Path("/b"), outDir / "b.js", Nil),
                       Source(Path("/c"), outDir / "c.js", Nil)
                     ))
    val rule       = Rules.Coffee(outDir, inSources)

    it("should return sources") {
      rule.sources must equal (outSources.sources)
      outSources.sources.map(_.file.exists).foldLeft(false)(_ || _) must equal (false)
    }

    it("should compile sources") {
      assertNotExists(outDir / "a.js")
      assertChange(outDir / "a.js") {
        rule.compile()
      }
      assertContents(outDir / "a.js") {
        """
        |// Generated by CoffeeScript 1.6.2
        |alert('a');
        |
        """.trim.stripMargin
      }
      assertNoop(outDir / "a.js") {
        rule.compile()
      }
    }
  }

  describe("Rewrite") {
    val inDir      = createTemporaryFiles(
                       "a.txt" -> "a",
                       "b.txt" -> "b",
                       "c.txt" -> "c"
                     )
    val inSources  = Selectors.Const(List(
                       Source(Path("/a"), inDir / "a.txt", Nil),
                       Source(Path("/b"), inDir / "b.txt", Nil),
                       Source(Path("/c"), inDir / "c.txt", Nil)
                     ))
    val outDir     = IO.createTemporaryDirectory
    val outSources = Selectors.Const(List(
                       Source(Path("/a"), outDir / "a.txt", Nil),
                       Source(Path("/b"), outDir / "b.txt", Nil),
                       Source(Path("/c"), outDir / "c.txt", Nil)
                     ))
    val rule       = Rules.Rewrite(
                       outDir,
                       (in: Source, contents: String) => "[header " + in.path + "]" + contents + "[footer " + in.path + "]",
                       inSources
                     )

    it("should return sources") {
      rule.sources must equal (outSources.sources)
      assertNotExists(outDir / "a.txt")
    }

    it("should compile sources") {
      assertNotExists(outDir / "a.txt")
      assertChange(outDir / "a.txt") {
        rule.compile()
      }
      assertContents(outDir / "a.txt")("[header /a]a[footer /a]")
      assertNoop(outDir / "a.txt") {
        rule.compile()
      }
    }
  }

  describe("UglifyJs") {
    val inDir      = createTemporaryFiles(
                       "a.txt" -> "a",
                       "b.txt" -> "b",
                       "c.txt" -> "c"
                     )
    val inSources  = Selectors.Const(List(
                       Source(Path("/a"), inDir / "a.txt", Nil),
                       Source(Path("/b"), inDir / "b.txt", Nil),
                       Source(Path("/c"), inDir / "c.txt", Nil)
                     ))
    val outDir     = IO.createTemporaryDirectory
    val outSources = Selectors.Const(List(
                       Source(Path.Root, outDir / "out.txt", Nil)
                     ))
    val rule       = Rules.UglifyJs(outDir / "out.txt", inSources)

    it("should return sources") {
      rule.sources must equal (outSources.sources)
      assertNotExists(outDir / "out.txt")
    }

    it("should compile sources") {
      assertNotExists(outDir / "out.txt")
      assertChange(outDir / "out.txt") {
        rule.compile()
      }
      assertContents(outDir / "out.txt")("a;b;c;")
      assertNoop(outDir / "out.txt") {
        rule.compile()
      }
    }
  }
}