local stockKey = KEYS[1]
local orderKey = KEYS[2]
local userId = ARGV[1]

local stock = tonumber(redis.call('get', stockKey) or '-1')
if stock <= 0 then
  return 1
end

if redis.call('sismember', orderKey, userId) == 1 then
  return 2
end

redis.call('decr', stockKey)
redis.call('sadd', orderKey, userId)
return 0
