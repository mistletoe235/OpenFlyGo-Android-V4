import argparse
from pathlib import Path


BASE = "app/src/main/java/edu/playground/djivln/"
TREES = ("camera", "survey", "hil")
FILES = (
    "mini2/Mini2CameraActivity.java",
    "mini2/Mini2AircraftBridge.kt",
    "mini2/CallbackGeneration.kt",
)


def main():
    parser = argparse.ArgumentParser(description="Compare shared V4 mission fixes without copying private features.")
    parser.add_argument("--private-root", required=True, type=Path)
    args = parser.parse_args()
    public = Path(__file__).resolve().parents[1]
    private = args.private_root.resolve()
    paths = {BASE + name for name in FILES}
    errors = []
    for tree in TREES:
        for root in (public, private):
            directory = root / BASE / tree
            if not directory.is_dir():
                errors.append(f"MISSING TREE: {directory}")
            paths.update(str(path.relative_to(root)) for path in directory.rglob("*")
                         if path.suffix in (".java", ".kt"))
    passed = 0
    for path in sorted(paths):
        try:
            if (public / path).read_text() != (private / path).read_text():
                errors.append(f"DIFF: {path}")
            else:
                passed += 1
        except OSError:
            errors.append(f"MISSING: {path}")
    for error in errors:
        print(error)
    print(f"Shared checks passed: {passed}; failed: {len(errors)}")
    print("MANUAL REVIEW: model stubs, cloud endpoint/preview policy, release configuration and remaining UI/tests.")
    return 1 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
