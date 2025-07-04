export default defineEventHandler((event) => {
  const url = event.node.req.url || "";
  if (url.startsWith("/.well-known/")) {
    event.node.res.statusCode = 404;
    event.node.res.end("Not Found");
  }
});
