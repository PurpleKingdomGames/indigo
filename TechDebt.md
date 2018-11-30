# Technical debt / Things I think should be different

- AudioPlayer should be a subsystem
- Networking should be subsystem

What to do about the nasty registerAnimation and registerFont methods - they perform an important function:
They allow you to register animations and fonts during the setup function. Technically we want the same for subsystems, but it uses a lot of mutable nasty...